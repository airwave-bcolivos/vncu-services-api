package com.vincu.core_services.complaint.service;

import com.vincu.core_services.complaint.domain.Complaint;
import com.vincu.core_services.complaint.domain.ComplaintStatus;
import com.vincu.core_services.complaint.dto.ComplaintRequest;
import com.vincu.core_services.complaint.dto.ComplaintResponse;
import com.vincu.core_services.complaint.repository.ComplaintRepository;
import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.users.domain.User;
import com.vincu.core_services.users.repository.ClientProfileRepository;
import com.vincu.core_services.users.repository.TechnicianProfileRepository;
import com.vincu.core_services.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            ClientProfileRepository clientProfileRepository,
                            TechnicianProfileRepository technicianProfileRepository){
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.technicianProfileRepository = technicianProfileRepository;
    }

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El usuario no existe"));

        Complaint complaint = Complaint.builder()
                .userId(user.getId())
                .title(request.title().trim())
                .description(request.description().trim())
                .build();

        Complaint saved = complaintRepository.save(complaint);

        return new ComplaintResponse(
              saved.getComplaintId(),
              saved.getUserId(),
              resolveName(user),
              user.getEmail(),
              user.getRole(),
              saved.getTitle(),
              saved.getDescription(),
              saved.getStatus(),
              saved.getCreatedAt()

        );
    }

    public PageResponse<ComplaintResponse> getAllComplaints(ComplaintStatus status, int page, int size) {
        Pageable pageable = buildPageable(page, size);

        Page<Complaint> result = (status == null)
                ? complaintRepository.findAll(pageable)
                : complaintRepository.findByStatus(status, pageable);

        return toResponsePage(result);
    }

    public PageResponse<ComplaintResponse> getComplaintsByUser(UUID userId, ComplaintStatus status, int page, int size) {
        Pageable pageable = buildPageable(page, size);

        Page<Complaint> result = (status == null)
                ? complaintRepository.findByUserId(userId, pageable)
                : complaintRepository.findByUserIdAndStatus(userId, status, pageable);

        return toResponsePage(result);
    }

    // Un solo usuario (para el POST)
    private String resolveName(User user) {
        return switch (user.getRole()) {
            case "CLIENT" -> clientProfileRepository.findById(user.getId())
                    .map(p -> p.getNames() + " " + p.getLastname())
                    .orElse(null);
            case "TECHNICIAN" -> technicianProfileRepository.findById(user.getId())
                    .map(p -> p.getNames() + " " + p.getLastname())
                    .orElse(null);
            default -> null; // por ejemplo, un administrador sin perfil
        };
    }

    // Varios usuarios de una página, en bloque (evita una consulta por fila)
    private Map<UUID, String> resolveNames(List<Complaint> complaints) {
        Set<UUID> clientIds = new HashSet<>();
        Set<UUID> technicianIds = new HashSet<>();

        for (Complaint c : complaints) {
            String role = c.getUser().getRole();
            if ("CLIENT".equals(role)) clientIds.add(c.getUserId());
            else if ("TECHNICIAN".equals(role)) technicianIds.add(c.getUserId());
        }

        Map<UUID, String> names = new HashMap<>();
        clientProfileRepository.findAllById(clientIds)
                .forEach(p -> names.put(p.getUserId(), p.getNames() + " " + p.getLastname()));
        technicianProfileRepository.findAllById(technicianIds)
                .forEach(p -> names.put(p.getUserId(), p.getNames() + " " + p.getLastname()));

        return names;
    }

    private PageResponse<ComplaintResponse> toResponsePage(Page<Complaint> page) {
        Map<UUID, String> names = resolveNames(page.getContent());

        return PageResponse.from(page.map(c -> new ComplaintResponse(
                c.getComplaintId(),
                c.getUserId(),
                names.get(c.getUserId()),
                c.getUser().getEmail(),
                c.getUser().getRole(),
                c.getTitle(),
                c.getDescription(),
                c.getStatus(),
                c.getCreatedAt()
        )));
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
    }
}
