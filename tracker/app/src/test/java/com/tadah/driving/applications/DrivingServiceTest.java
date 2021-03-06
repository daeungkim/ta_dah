package com.tadah.driving.applications;

import com.tadah.driving.domains.entities.Driving;
import com.tadah.driving.domains.repositories.DrivingRepository;
import com.tadah.driving.domains.repositories.infra.JpaDrivingRepository;
import com.tadah.driving.utils.CoordinateUtil;
import org.geolatte.geom.C2D;
import org.geolatte.geom.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static com.tadah.driving.domains.entities.DrivingTest.AFTER_MAP_MATCH;
import static com.tadah.driving.domains.entities.DrivingTest.DRIVING;
import static com.tadah.driving.domains.entities.DrivingTest.LATITUDE;
import static com.tadah.driving.domains.entities.DrivingTest.LONGITUDE;
import static com.tadah.driving.domains.entities.DrivingTest.POINT;
import static com.tadah.driving.domains.entities.DrivingTest.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("DrivingService 클래스")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DrivingServiceTest {
    private final DrivingService drivingService;
    private final DrivingRepository drivingRepository;
    private final JpaDrivingRepository jpaDrivingRepository;

    public DrivingServiceTest(
        @Autowired final DrivingRepository drivingRepository,
        @Autowired final JpaDrivingRepository jpaDrivingRepository) throws FactoryException {
        this.drivingRepository = drivingRepository;
        this.jpaDrivingRepository = jpaDrivingRepository;
        this.drivingService = new DrivingService(new CoordinateUtil(), drivingRepository);
    }

    @Nested
    @DisplayName("transForm 메서드는")
    public final class Describe_transFrom {
        private Point<C2D> subject() throws TransformException {
            return drivingService.transForm(LATITUDE, LONGITUDE);
        }

        @Test
        @DisplayName("좌표계 변환 후 맵매칭 수행 결과를 리턴한다")
        public void it_returns_the_result_of_map_matching_after_coordinate_system_transformation() throws TransformException {
            assertThat(subject())
                .matches(point -> point.getPosition().getX() == AFTER_MAP_MATCH.getPosition().getX())
                .matches(point -> point.getPosition().getY() == AFTER_MAP_MATCH.getPosition().getY());
        }
    }

    @Nested
    @DisplayName("get 메서드는")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public final class Describe_get {
        private Driving driving;

        private Optional<Driving> subject() {
            return drivingService.get(USER_ID);
        }

        @BeforeAll
        private void beforeAll() {
            driving = drivingRepository.save(DRIVING);
        }

        @AfterAll
        private void afterAll() {
            jpaDrivingRepository.deleteAll();
        }

        @Test
        @DisplayName("운행정보를 찾는다")
        public void it_finds_the_driving_data() {
            assertThat(subject())
                .isPresent();
        }

        @Nested
        @DisplayName("운행이 종료된 경우")
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        public final class Context_stopDriving {
            @BeforeAll
            private void beforeAll() {
                driving.stopDriving();
                drivingRepository.save(driving);
            }

            @Test
            @DisplayName("운행이 종료되었음을 알려준다")
            public void it_notifies_that_driving_is_finished() {
                assertThat(subject())
                    .isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("start 메서드는")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public final class Describe_start {
        private Driving subject() {
            return drivingService.start(DRIVING);
        }

        @AfterAll
        private void afterAll() {
            jpaDrivingRepository.deleteAll();
        }

        @Test
        @DisplayName("운행을 시작한다")
        public void it_starts_the_driving() {
            assertThat(subject())
                .matches(driving -> driving.getUserId().equals(USER_ID));
        }
    }

    @Nested
    @DisplayName("stop 메서드는")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public final class Describe_stop {
        private Driving driving;

        private void subject() {
            drivingService.stop(driving, POINT);
        }

        @BeforeAll
        private void beforeAll() {
            driving = drivingService.start(DRIVING);
        }

        @AfterAll
        private void afterAll() {
            jpaDrivingRepository.deleteAll();
        }

        @Test
        @DisplayName("운행을 시작한다")
        public void it_starts_the_driving() {
            subject();

            assertThat(jpaDrivingRepository.findById(driving.getId()))
                .isPresent()
                .get()
                .matches(driving -> !driving.isDriving());
        }
    }

    @Nested
    @DisplayName("update 메서드는")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public final class Describe_update {
        private Driving driving;

        private void subject() {
            drivingService.update(driving, POINT);
        }

        @BeforeAll
        private void beforeAll() {
            driving = drivingRepository.save(DRIVING);
        }

        @AfterAll
        private void afterAll() {
            jpaDrivingRepository.deleteAll();
        }

        @Test
        @DisplayName("위치정보를 업데이트한다")
        public void it_updates_the_location_data() {
            subject();

            assertThat(jpaDrivingRepository.findById(driving.getId()))
                .isPresent()
                .get()
                .matches(driving -> driving.getPath().getEndPosition().equals(POINT.getPosition()));
        }
    }
}
