package kotdata.jdbc

data class Page<out E>(
        /** 数据库表中的数据总量 */
        val rowNum: Int = 0,
        /** 按照当前页大小计算出的总页数 */
        val pageNum: Int = 0,
        /** 当前页数 */
        val currentPage: Int = 0,
        /** 当前页的数据 */
        val pageData: List<E>) {
    companion object {
        fun <E> empty(): Page<E> = Page(0, 0, 0, emptyList())
    }
}