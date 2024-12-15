package soma.achoom.zigg.report.entity

import jakarta.persistence.*
import soma.achoom.zigg.global.BaseEntity

@Entity
class Report(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,

    @Enumerated(EnumType.STRING)
    val reportType: ReportType,

    val reportMessage: String,

    val reportSpecific : String


) : BaseEntity()