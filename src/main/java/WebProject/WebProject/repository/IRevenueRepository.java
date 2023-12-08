package WebProject.WebProject.repository;

import WebProject.WebProject.entity.RevenueStatic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IRevenueRepository extends JpaRepository<RevenueStatic, Long> {
    @Query("SELECT NEW WebProject.WebProject.entity.RevenueStatic(" +
            "    o.booking_Date, " +
            "    SUM(CAST(oi.count AS java.lang.Long))" +
            ") " +
            "FROM Order o " +
            "JOIN Order_Item oi ON o.id = oi.order.id " +
            "WHERE o.status = 'Hoàn tất' AND o.booking_Date BETWEEN :startDate AND :endDate " +
            "GROUP BY o.booking_Date")
    List<RevenueStatic> findRevenueByDateRangeAndStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
