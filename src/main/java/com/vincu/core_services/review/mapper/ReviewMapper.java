package com.vincu.core_services.review.mapper;

import com.vincu.core_services.jobs.domain.JobOffer;
import com.vincu.core_services.review.domain.ClientReview;
import com.vincu.core_services.review.domain.JobReview;
import com.vincu.core_services.review.dto.ClientReviewResponse;
import com.vincu.core_services.review.dto.JobReviewResponse;
import com.vincu.core_services.users.domain.ClientProfile;
import com.vincu.core_services.users.domain.TechnicianProfile;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    // --- Mapeos para reseñas de Técnicos (JobReview) ---

    public JobReviewResponse toJobReviewResponse(JobReview review) {
        return toJobReviewResponse(review, review.getJobOffer(), review.getTechnician());
    }

    public JobReviewResponse toJobReviewResponse(JobReview review, JobOffer offer, TechnicianProfile technician) {
        return new JobReviewResponse(
                review.getId(),
                review.getJobOfferId(),
                offer != null ? offer.getTitle() : null,
                review.getTechnicianId(),
                technician != null ? technician.getNames() : null,
                technician != null ? formatFullLastName(technician.getLastname(), technician.getSecondLastname()) : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    // --- Mapeos para reseñas de Clientes (ClientReview) ---

    public ClientReviewResponse toClientReviewResponse(ClientReview review) {
        return toClientReviewResponse(review, review.getJobOffer(), review.getClient());
    }

    public ClientReviewResponse toClientReviewResponse(ClientReview review, JobOffer offer, ClientProfile client) {
        return new ClientReviewResponse(
                review.getId(),
                review.getJobOfferId(),
                offer != null ? offer.getTitle() : null,
                review.getClientId(),
                client != null ? client.getNames() : null,
                client != null ? formatFullLastName(client.getLastname(), client.getSecondLastname()) : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    // --- Helper compartido ---

    private String formatFullLastName(String lastname, String secondLastname) {
        if (secondLastname == null || secondLastname.isBlank()) {
            return lastname;
        }
        return lastname + " " + secondLastname;
    }
}