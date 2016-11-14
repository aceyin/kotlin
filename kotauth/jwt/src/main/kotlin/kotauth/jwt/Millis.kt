package kotauth.jwt

/**
 * Created by ace on 2016/11/14.
 */

enum class Millis(val mills: Long) {
    /* 半分钟 */
    HALF_MIN(30000L),
    /* 一分钟 */
    ONE_MIN(60000L),
    /* 5分钟 */
    FIVE_MIN(300000L),
    /* 10分钟 */
    TEN_MIN(600000L),
    /* 15分钟 */
    ONE_QUARTER(900000L),
    /* 半小时 */
    HALF_HOUR(1800000L),
    /* 1小时 */
    ONE_HOUR(3600000L),
    /* 半天 */
    HALF_DAY(43200000L),
    /* 1天 */
    ONE_DAY(86400000L),
    /* 1周 */
    ONE_WEEK(604800000L),
    /* 1个月 */
    ONE_MONTH(2592000000L)
}