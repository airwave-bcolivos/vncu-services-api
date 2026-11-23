package com.vincu.core_services.jobs.mapper;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.JobOfferDetailResponse;
import com.vincu.core_services.jobs.dto.JobOfferResponse;
import com.vincu.core_services.jobs.dto.JobOfferSummaryResponse;
import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.domain.TechnicianProfile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JobOfferMapper {
    public JobOfferResponse toResponse(JobOffer saved, ClientProfile client) {
        return new JobOfferResponse(
                saved.getId(),
                saved.getClientId(),
                saved.getTitle(),
                saved.getSpecialty(),
                saved.getDescription(),
                saved.getMaxBudget(),
                saved.getStatus(),
                saved.getUbigeoDistrictId(),
                saved.getLatitude(),
                saved.getLongitude(),
                client != null ? client.getAverageRating() : null,
                client != null ? client.getRatingCount() : 0,
                saved.getCreatedAt()
        );
    }

    public JobOfferSummaryResponse toSummary(JobOffer o) {
        ClientProfile client = o.getClient();
        return new JobOfferSummaryResponse(
                o.getId().toString(),
                o.getTitle(),
                o.getSpecialty(),
                o.getMaxBudget(),
                o.getStatus(),
                o.getUbigeoDistrictId(),
                districtName(o),
                client != null ? client.getAverageRating() : null,
                client != null ? client.getRatingCount() : 0,
                o.getCreatedAt()
        );
    }

    public JobOfferDetailResponse toDetail(JobOffer jobOffer, UUID viewerId) {
        // TODO: Pensar como hacer para que no falle cuando se agregue el administrador (id admin)
        boolean isOwner = viewerId != null && jobOffer.getClientId().equals(viewerId);
        boolean isAssignedTechnician = viewerId != null
                && (jobOffer.getStatus() == JobStatus.IN_PROGRESS || jobOffer.getStatus() == JobStatus.COMPLETED)
                && jobOffer.getTechnicianId() != null
                && jobOffer.getTechnicianId().equals(viewerId);

        boolean canSeePrivateData = isOwner || isAssignedTechnician;

        ClientProfile client = jobOffer.getClient();
        String clientName = canSeePrivateData ? fullName(client) : null;
        String clientPhone = (canSeePrivateData && client != null) ? client.getPhoneNumber() : null;
        String address = canSeePrivateData ? jobOffer.getSpecificAddress() : null;
        String technicianName = jobOffer.getTechnician() != null ? fullName(jobOffer.getTechnician()) : null;

        var district = jobOffer.getDistrict();
        var province = district != null ? district.getProvince() : null;
        var department = province != null ? province.getDepartment() : null;

        return new JobOfferDetailResponse(
                jobOffer.getId(),
                jobOffer.getClientId(),
                clientName,
                clientPhone,
                jobOffer.getTechnicianId(),
                technicianName,
                jobOffer.getTitle(),
                jobOffer.getSpecialty(),
                jobOffer.getDescription(),
                jobOffer.getMaxBudget(),
                jobOffer.getStatus(),
                jobOffer.getUbigeoDistrictId(),
                districtName(jobOffer),
                district != null ? district.getProvinceId() : null,
                province != null ? province.getName() : null,
                province != null ? province.getDepartmentId() : null,
                department != null ? department.getName() : null,
                address,
                jobOffer.getLatitude(),
                jobOffer.getLongitude(),
                client != null ? client.getAverageRating() : null,
                client != null ? client.getRatingCount() : 0,
                jobOffer.getCreatedAt(),
                jobOffer.getUpdatedAt()
        );
    }

    private String fullName(ClientProfile c) {
        return c == null ? null : c.getNames() + " " + c.getLastname();
    }

    private String fullName(TechnicianProfile t) {
        return t == null ? null : t.getNames() + " " + t.getLastname();
    }

    private String districtName(JobOffer o) {
        return o.getDistrict() != null ? o.getDistrict().getName() : null;
    }
}
