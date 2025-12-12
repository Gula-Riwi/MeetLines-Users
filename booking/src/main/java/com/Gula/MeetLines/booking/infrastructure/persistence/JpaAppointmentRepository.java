package com.Gula.MeetLines.booking.infrastructure.persistence;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import com.Gula.MeetLines.booking.domain.AppointmentStatus;
import com.Gula.MeetLines.booking.infrastructure.persistence.entity.AppointmentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA implementation of the AppointmentRepository domain interface.
 * 
 * <p>
 * This is an <strong>Adapter</strong> in Hexagonal Architecture, which means:
 * </p>
 * <ul>
 * <li>It's in the INFRASTRUCTURE layer</li>
 * <li>It implements a domain interface (AppointmentRepository)</li>
 * <li>It adapts Spring Data JPA to the domain's needs</li>
 * <li>It handles the mapping between domain and JPA entities</li>
 * </ul>
 * 
 * <p>
 * <strong>Architecture pattern:</strong>
 * </p>
 * 
 * <pre>
 *     Domain Interface (AppointmentRepository)
 *            ↑ implements
 *     JpaAppointmentRepository (this class)
 *            ↓ delegates to
 *     SpringDataJpaRepository (Spring Data interface)
 *            ↓ uses
 *     AppointmentEntity (JPA entity)
 * </pre>
 * 
 * <p>
 * <strong>Responsibilities:</strong>
 * </p>
 * <ul>
 * <li>Implement all methods from AppointmentRepository interface</li>
 * <li>Delegate to Spring Data JPA repository</li>
 * <li>Map between domain Appointment and JPA AppointmentEntity</li>
 * <li>Provide custom queries when needed</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Repository
@RequiredArgsConstructor
public class JpaAppointmentRepository implements AppointmentRepository {

        /**
         * Spring Data JPA repository for AppointmentEntity.
         * 
         * <p>
         * This is a Spring Data interface that provides:
         * </p>
         * <ul>
         * <li>Basic CRUD operations (save, findById, delete, etc.)</li>
         * <li>Custom query methods (defined below)</li>
         * <li>Automatic implementation by Spring</li>
         * </ul>
         */
        private final SpringDataAppointmentRepository springDataRepository;

        @Override
        public Appointment save(Appointment appointment) {
                // Convert domain → entity
                AppointmentEntity entity = AppointmentEntity.fromDomain(appointment);

                // Save using Spring Data JPA
                AppointmentEntity savedEntity = springDataRepository.save(entity);

                // Convert entity → domain
                return savedEntity.toDomain();
        }

        @Override
        public Optional<Appointment> findById(Long id) {
                return springDataRepository.findById(id)
                                .map(AppointmentEntity::toDomain);
        }

        @Override
        public List<Appointment> findByUserId(UUID userId) {
                return springDataRepository.findByAppUserId(userId)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByProjectId(UUID projectId) {
                return springDataRepository.findByProjectId(projectId)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByStatus(AppointmentStatus status) {
                return springDataRepository.findByStatus(status)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findPendingAppointmentsToStart(ZonedDateTime currentTime) {
                return springDataRepository.findPendingAppointmentsToStart(currentTime)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findInProgressAppointmentsToComplete(ZonedDateTime currentTime) {
                return springDataRepository.findInProgressAppointmentsToComplete(currentTime)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByProjectIdAndDateRange(
                        UUID projectId,
                        ZonedDateTime startDate,
                        ZonedDateTime endDate) {
                return springDataRepository.findByProjectIdAndDateRange(projectId, startDate, endDate)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public boolean isTimeSlotAvailable(
                        UUID projectId,
                        ZonedDateTime startTime,
                        ZonedDateTime endTime,
                        Long excludeId) {
                List<AppointmentEntity> conflicts = springDataRepository.findConflictingAppointments(
                                projectId,
                                startTime,
                                endTime,
                                excludeId);

                return conflicts.isEmpty();
        }

        @Override
        public void deleteById(Long id) {
                springDataRepository.deleteById(id);
        }

        @Override
        public long countByProjectIdAndStatus(UUID projectId, AppointmentStatus status) {
                return springDataRepository.countByProjectIdAndStatus(projectId, status);
        }

        @Override
        public List<Appointment> findByProjectIdAndDate(UUID projectId, java.time.LocalDate date) {
                return springDataRepository.findByProjectIdAndDate(projectId, date)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Appointment> findByEmployeeIdAndDate(UUID employeeId, java.time.LocalDate date) {
                return springDataRepository.findByEmployeeIdAndDate(employeeId, date)
                                .stream()
                                .map(AppointmentEntity::toDomain)
                                .collect(Collectors.toList());
        }

}
