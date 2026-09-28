package com.sfms.mf12.service;

import com.sfms.mf12.dto.SystemDtos.BackupsResponse;
import com.sfms.mf12.dto.SystemDtos.ServiceHealth;
import com.sfms.mf12.dto.SystemDtos.SystemStatusResponse;
import com.sfms.mf12.repository.ExternalIntegrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemStatusServiceImpl implements SystemStatusService {

    private final ExternalIntegrationRepository integrationRepository;
    private final BackupService backupService;

    public SystemStatusServiceImpl(ExternalIntegrationRepository integrationRepository, BackupService backupService) {
        this.integrationRepository = integrationRepository;
        this.backupService = backupService;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatusResponse healthChecks() {
        List<ServiceHealth> services = integrationRepository.findAllByOrderByLabelAsc().stream()
                .map(i -> new ServiceHealth(i.getCode(), i.getLabel(), i.isConnected(), i.getStatusNote(),
                        i.getLastTestedAt()))
                .toList();
        boolean allUp = services.stream().allMatch(ServiceHealth::connected);
        // BR-160/161 — nay có BackupScheduler thật (V35/BackupServiceImpl), khác bản cũ luôn false.
        return new SystemStatusResponse(allUp ? "UP" : "DEGRADED", services, true);
    }

    @Override
    public BackupsResponse backups() {
        return backupService.list();
    }
}
