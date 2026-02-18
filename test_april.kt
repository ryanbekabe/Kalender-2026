import java.util.*
import java.util.TimeZone

fun main() {
    val timezone = TimeZone.getTimeZone("Asia/Jakarta")
    val calendar = Calendar.getInstance(timezone, Locale.US)
    val currentYear = 2026
    
    // Test April (month index 3)
    val monthIndex = 3
    calendar.set(currentYear, monthIndex, 1)
    
    println("April 2026:")
    println("First day of month: ${calendar.get(Calendar.DAY_OF_WEEK)}") // 1=Sunday, 4=Wednesday
    println("Days in month: ${calendar.getActualMaximum(Calendar.DAY_OF_MONTH)}")
    
    // Check specific dates
    for (day in 1..30) {
        calendar.set(currentYear, monthIndex, day)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayName = when(dayOfWeek) {
            Calendar.SUNDAY -> "Minggu"
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            else -> "Unknown"
        }
        if (day == 1 || day == 3 || day == 5 || day == 30) {
            println("April $day, 2026 = $dayName")
        }
    }
}
