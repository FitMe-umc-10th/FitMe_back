package umc.fitme.domain.post.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * ScholarshipAmountParser 수정 이전에 저장된 support_amount_value를 교정하는 일회성 백필.
 * <p>
 * 외부 공공 API를 다시 호출하지 않고, DB에 이미 저장된 원본 문자열(support_amount)만 재파싱한다.
 * active 여부와 무관하게 전체 장학금이 대상.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipAmountBackfillService {

    private static final int PAGE_SIZE = 500;

    private final ScholarshipAmountBackfillWriter scholarshipAmountBackfillWriter;

    /**
     * 전체 장학금의 supportAmountValue를 원본 문자열로 다시 계산한다.
     * 수천 건을 한 번에 메모리에 올리지 않도록 500건씩 페이지 단위로 끊어 처리하며,
     * 페이지마다 별도 트랜잭션으로 커밋한다.
     *
     * @return 처리한 총 건수
     */
    public int backfillAll() {
        long total = scholarshipAmountBackfillWriter.countAll();
        log.info("장학금 supportAmountValue 백필 시작 - 대상:{}건, 페이지크기:{}", total, PAGE_SIZE);

        int processedCount = 0;
        int pageNumber = 0;

        while (true) {
            // 페이지마다 트랜잭션이 끊기므로 정렬을 고정해야 행이 밀려 누락되지 않는다.
            int pageCount = scholarshipAmountBackfillWriter.refreshPage(
                    PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id")));

            if (pageCount == 0) {
                break;
            }

            processedCount += pageCount;
            pageNumber++;
            log.info("장학금 supportAmountValue 백필 진행 - 누적 {}건 처리", processedCount);
        }

        log.info("장학금 supportAmountValue 백필 완료 - 총 {}건 처리", processedCount);
        return processedCount;
    }
}