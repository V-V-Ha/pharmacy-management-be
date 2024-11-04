package com.fu.pha.repository;

import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByPhoneNumber(String phoneNumber);

    Optional<Doctor> findById(Long id);

    @Query("SELECT new com.fu.pha.dto.response.DoctorDTOResponse(d) FROM Doctor d WHERE " +
            "(LOWER(d.fullName) LIKE LOWER(CONCAT('%', :doctorName, '%')) OR :doctorName IS NULL OR :doctorName = '') " +
            "ORDER BY d.lastModifiedDate DESC")
    Page<DoctorDTOResponse> getListDoctorPaging(String doctorName, Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.DoctorDTOResponse(d) FROM Doctor d WHERE " +
            "(LOWER(d.fullName) LIKE LOWER(CONCAT('%', :doctorName, '%')))")
    Optional<List<DoctorDTOResponse>> findByDoctorName(String doctorName);
}
