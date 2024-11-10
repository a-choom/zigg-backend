package soma.achoom.zigg.feedback.dto

import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull


data class FeedbackRequestDto(
    val feedbackTimeline: String?,
    val feedbackMessage: String?,
    @NotNull
    @Min(value = 1, message = "받는 사람은 최소 1명 이상이어야 합니다.")
    val recipientId: MutableSet<Long> = mutableSetOf(),
) {

}