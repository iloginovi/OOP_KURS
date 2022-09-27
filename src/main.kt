import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        val panel = CourseWorkUi()
        panel.setLocationRelativeTo(null)
        panel.isVisible = true
    }
}