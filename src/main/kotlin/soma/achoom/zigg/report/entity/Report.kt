package soma.achoom.zigg.report.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import soma.achoom.zigg.global.BaseEntity

@Entity
class Report(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    var id: Long? = null,
    @Column(name = "reporter_user_id")
    var reporter : Long,
    @Column(name = "report_type")
    var type : String,
):BaseEntity() {

}