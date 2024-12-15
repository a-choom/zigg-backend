package soma.achoom.zigg.report.repository

import org.springframework.data.jpa.repository.JpaRepository
import soma.achoom.zigg.report.entity.Report

interface ReportRepository : JpaRepository<Report, Long>{

}