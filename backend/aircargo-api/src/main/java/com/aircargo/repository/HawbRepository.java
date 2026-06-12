package com.aircargo.repository;

import com.aircargo.entity.Hawb;
import com.aircargo.entity.MawbStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HawbRepository extends JpaRepository<Hawb, UUID>{

    List<Hawb> findByAirlineId(UUID airlineId);

    List<Hawb> findByMawbId(UUID mawbId);

    List<Hawb> findByAirlineIdAndStatus(UUID airlineId, MawbStatus status);

    //Metodos avanzados --- se agregaran despues de corregir la entidad
}