package dev.omniexx.service;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.CompanyEvent;
import dev.omniexx.repository.CompanyEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final CompanyEventRepository eventRepo;

    @Transactional
    public void log(Company company, String type, String title, String description,
                    Map<String, Long> kpiDelta) {
        CompanyEvent event = CompanyEvent.builder()
                .company(company)
                .tickNumber(company.getTickCount())
                .eventType(type)
                .title(title)
                .description(description)
                .kpiDelta(kpiDelta)
                .build();
        eventRepo.save(event);
    }

    public List<CompanyEvent> getRecentEvents(Long companyId, int limit) {
        return eventRepo.findByCompanyIdOrderByOccurredAtDesc(
                companyId, PageRequest.of(0, limit));
    }
}
