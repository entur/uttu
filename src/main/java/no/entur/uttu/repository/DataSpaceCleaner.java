/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

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
