package no.entur.uttu.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataSpaceCleaner {

    @Autowired
    private FlexibleLineRepository flexibleLineRepository;

    @Autowired
    private FlexibleStopPlaceRepository flexibleStopPlaceRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private NoticeRepository noticeRepository;


    public void clean() {
        flexibleLineRepository.deleteAll();
        flexibleStopPlaceRepository.deleteAll();
        networkRepository.deleteAll();
        noticeRepository.deleteAll();
    }
}
