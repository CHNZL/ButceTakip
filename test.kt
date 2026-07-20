fun main() {
    val d = 6055.010677
    val s1 = String.format(java.util.Locale("tr"), "%,.2f", d)
    val s2 = String.format(java.util.Locale("tr", "TR"), "%,.2f", d)
    println("tr: " + s1)
    println("tr_TR: " + s2)
}
