package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBooker_IdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByBooker_IdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBooker_IdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2
    );

    boolean existsByBooker_IdAndItem_IdAndStatusAndEndBefore(Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 order by b.start desc")
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.end < ?2 order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start > ?2 order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start < ?2 and b.end > ?3 order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now1, LocalDateTime now2
    );

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2 order by b.start desc")
    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);
}
