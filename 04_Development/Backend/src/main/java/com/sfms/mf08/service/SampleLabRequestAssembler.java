package com.sfms.mf08.service;

import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.AttachmentOwner;
import com.sfms.mf08.domain.SampleLabRequest;
import com.sfms.mf08.dto.CommonDtos.UploadedFileDto;
import com.sfms.mf08.dto.SampleLabRequestDtos.SampleLabRequestResponse;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf08.repository.ReceivingAuthorityRepository;
import com.sfms.mf08.repository.SampleAttachmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SampleLabRequestAssembler {

    private final FoodSampleRepository foodSampleRepository;
    private final ReceivingAuthorityRepository receivingAuthorityRepository;
    private final SampleAttachmentRepository attachmentRepository;

    public SampleLabRequestAssembler(FoodSampleRepository foodSampleRepository,
                                     ReceivingAuthorityRepository receivingAuthorityRepository,
                                     SampleAttachmentRepository attachmentRepository) {
        this.foodSampleRepository = foodSampleRepository;
        this.receivingAuthorityRepository = receivingAuthorityRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public SampleLabRequestResponse toResponse(SampleLabRequest r) {
        List<String> sampleIds = foodSampleRepository.findByLabRequestId(r.getId()).stream()
                .map(s -> s.getCode()).toList();
        String authorityId = r.getAuthorityId() == null ? null
                : receivingAuthorityRepository.findById(r.getAuthorityId()).map(a -> a.getCode()).orElse(null);

        return new SampleLabRequestResponse(
                r.getCode(), r.getIncidentId() == null ? null : r.getIncidentId().toString(), sampleIds,
                r.getLabDestination(), authorityId, r.getHandoverType() == null ? null : r.getHandoverType().name(),
                r.getTestCriteria(), r.getRequestedBy(), r.getRequestedAt(), r.getDeliveredBy(), r.getSentToLabAt(),
                r.getLabRepresentative(), r.getStatus() == null ? null : r.getStatus().name(), r.getNote(),
                files(r, AttachmentKind.SEAL_PHOTO), files(r, AttachmentKind.HANDOVER_RECORD),
                files(r, AttachmentKind.LAB_RECEIPT));
    }

    private List<UploadedFileDto> files(SampleLabRequest r, AttachmentKind kind) {
        return attachmentRepository.findByOwnerTypeAndOwnerIdAndKindOrderByCreatedAtAsc(
                        AttachmentOwner.LAB_REQUEST, r.getId(), kind)
                .stream()
                .map(a -> new UploadedFileDto(a.getId().toString(), a.getFileName(), a.getCreatedAt(), a.getSizeBytes()))
                .toList();
    }
}
