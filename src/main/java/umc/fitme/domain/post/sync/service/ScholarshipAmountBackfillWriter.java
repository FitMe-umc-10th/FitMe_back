package umc.fitme.domain.post.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.fitme.domain.post.entity.Scholarship;
import umc.fitme.domain.post.repository.ScholarshipRepository;

/**
 * supportAmountValue 백필의 페이지 단위 쓰기 담당.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipAmountBackfillWriter {

    private final ScholarshipRepository scholarshipRepository;

    /**
     * 한 페이지 분량의 장학금을 다시 파싱해 supportAmountValue를 교정한다.
     *
     * @param pageable 조회할 페이지 (id 오름차순 고정)
     * @return 조회된 장학금 수(= 처리한 건수)
     */
    @Transactional
    public int refreshPage(Pageable pageable) {
        Page<Scholarship> page = scholarshipRepository.findAll(pageable);
        page.getContent().forEach(Scholarship::refreshSupportAmountValue);
        return page.getNumberOfElements();
    }

    /**
     * 전체 대상 건수.
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return scholarshipRepository.count();
    }
}