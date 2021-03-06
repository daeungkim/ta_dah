package com.tadah.driving.domains.repositories;

import com.tadah.driving.domains.entities.Driving;
import com.tadah.driving.dtos.PointData;
import org.geolatte.geom.C2D;
import org.geolatte.geom.Point;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;

public interface DrivingRepository {
    /**
     * 운행정보를 생성한다.
     *
     * @param driving 생성할 운행 정보
     * @return 생성한 운행정보
     */
    Driving save(final Driving driving);

    /**
     * 드라이버 아이디를 이용하여 운행정보를 조회한다.
     *
     * @param userId 드라이버 아이디
     * @return 운행 정보
     */
    @Query(value = "select * from driving where user_id = :userId and is_driving = true", nativeQuery = true)
    Optional<Driving> find(final Long userId);

    /**
     * 운행정보를 업데이트한다.
     *
     * @param id 업데이트할 운행 정보 아이디
     * @param point 현재 위치
     * @param isDriving 운행 상태
     */
    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "update driving " +
            "set path = st_addpoint((case when st_isempty(path) then st_addpoint(path, :point) else path end), :point), is_driving = :isDriving " +
            "where id = :id"
    )
    void update(
        @Param(value = "id") final Long id,
        @Param(value = "point") final Point<C2D> point,
        @Param(value = "isDriving") final boolean isDriving);

    /**
     * 맵매칭을 수행한다
     *
     * @param point 맵매칭을 수행할 위치
     * @return 맵매칭을 수행한 위치
     */
    @Query(nativeQuery = true)
    PointData mapMatch(@Param("point") final Point<C2D> point);
}
