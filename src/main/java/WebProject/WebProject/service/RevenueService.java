package WebProject.WebProject.service;


import WebProject.WebProject.entity.RevenueStatic;
import WebProject.WebProject.repository.IRevenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RevenueService {
    @Autowired
    private IRevenueRepository revenueRepository;

    public List<RevenueStatic> getRevenueStaticsInDateRangeAndStatus(LocalDate startDate, LocalDate endDate) {
        return revenueRepository.findRevenueByDateRangeAndStatus(startDate, endDate);
    }
}