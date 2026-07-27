package umc.fitme.domain.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.client.PublicDataApiClient;
import umc.fitme.domain.post.converter.PostConverter;
import umc.fitme.domain.post.dto.publicapi.PublicApiScholarshipDTO;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.repository.ScholarshipRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicDataSyncService {

    private final PublicDataApiClient publicDataApiClient;
    private final ScholarshipRepository scholarshipRepository;
    @Transactional
    public void syncScholarshipData(int page, int perPage) {
        log.info("공공데이터 장학금 API 연동 시작 - page: {}, perPage: {}", page, perPage);

        try {
            var response = publicDataApiClient.fetchScholarshipData(page, perPage);


            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                log.warn("API에서 가져올 장학금 데이터가 없습니다.");
                return;
            }

            List<PublicApiScholarshipDTO> dtoList = response.getData();


            List<Scholarship> scholarships = dtoList.stream()
                    .map(PostConverter::toScholarshipEntity)
                    .collect(Collectors.toList());

            scholarshipRepository.saveAll(scholarships);
            log.info("성공적으로 {}개의 장학금 데이터를 DB에 저장했습니다.", scholarships.size());

        } catch (Exception e) {
            log.error("장학금 데이터 동기화 중 오류 발생: {}", e.getMessage(), e);

        }
    }
}