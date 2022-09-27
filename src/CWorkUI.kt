import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridLayout
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDate
import javax.swing.*
import javax.swing.table.DefaultTableModel

private const val GAP = 15

class CourseWorkUi : JFrame("Notes") {
    private val statusLabel = JLabel("Notes", JLabel.CENTER)
    private var model = DefaultTableModel()
    private val table = JTable(model)

    //Куда записываются данные файлов
    private val content = mutableListOf<String>()
    private val task = mutableListOf<String>()
    private val lineList = mutableListOf<String>()
    private val numberTask = mutableListOf<String>()

    //конструктор главной панели
    init {
        setSize(400, 700)
        defaultCloseOperation = EXIT_ON_CLOSE
        //Запрет на редактирование
        model = object : DefaultTableModel() {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }
        }
        table.model = model
        val name: Array<String> = arrayOf("Вид заметки", "Название", "Дата создания")
        model.setColumnIdentifiers(name)
        model.addRow(name)

        //чтение с файла
        val inputStreamTable: InputStream = File("src/readTable.txt").inputStream()
        inputStreamTable.bufferedReader().forEachLine { lineList.add(it) }

        val inputStreamContent: InputStream = File("src/texts.txt").inputStream()
        inputStreamContent.bufferedReader().forEachLine { content.add(it) }

        val inputStreamTask: InputStream = File("src/task.txt").inputStream()
        inputStreamTask.bufferedReader().forEachLine { task.add(it) }

        val inputStreamNumberTask: InputStream = File("src/NumTask.txt").inputStream()
        inputStreamNumberTask.bufferedReader().forEachLine { numberTask.add(it) }

        //Заполение таблицы с файлов
        for (i in 0 until lineList.size step 3) {
            val data: Array<String> = arrayOf(lineList[i], lineList[i + 1], lineList[i + 2])
            model.addRow(data)
        }

        rootPane.contentPane = JPanel(BorderLayout(GAP, GAP)).apply {
            add(statusLabel, BorderLayout.NORTH)
            add(table, BorderLayout.CENTER)
            add(createButtons(), BorderLayout.SOUTH)
        }
    }

    //Кнопки на главной панели
    private fun createButtons(): Component {
        val createButton = JButton("+")
        val deleteButton = JButton("Remove")
        val changeButton = JButton("Editing")
        //добавление кнопок в одну структуру
        val grid = JPanel()
        val lay = GridLayout(1, 0, 5, 12)
        grid.layout = lay
        grid.add(createButton)
        grid.add(deleteButton)
        grid.add(changeButton)

        //Создание
        createButton.addActionListener {
            createSwitchWindow()
        }

        //Удаление
        deleteButton.addActionListener {
            val row = table.selectedRow * 3
            //Удаление Текстовой Заметки
            if (lineList[row - 3] == "Text Note") {
                var numberTaskBefore = 0
                for (i in 0..(row - 3)) {
                    if (lineList[i] == "To do list" || lineList[i] == "Image")
                        numberTaskBefore++
                }
                content.removeAt(row / 3 - numberTaskBefore - 1)
                File("src/texts.txt").bufferedWriter().use { out ->
                    for (i in 0 until content.size) {
                        out.write(content[i])
                        out.write("\n")
                    }
                }
            }

            //Удаление задач
            if (lineList[row - 3] == "To do list") {
                var numTask = 0
                for (i in 0..(row - 3)) {
                    if (lineList[i] == "To do list")
                        numTask++
                }

                var sum = 0
                for (i in 0 until numTask - 1) {
                    sum += 2 * numberTask[i].toInt()
                }

                for (i in 0 until numberTask[numTask - 1].toInt()) {
                    task.removeAt(sum)
                    task.removeAt(sum)
                }
                numberTask.removeAt(numTask - 1)

                File("src/task.txt").bufferedWriter().use { out ->
                    for (i in 0 until task.size) {
                        out.write(task[i])
                        out.write("\n")
                    }
                }
                File("src/NumTask.txt").bufferedWriter().use { out ->
                    for (i in 0 until numberTask.size) {
                        out.write(numberTask[i])
                        out.write("\n")
                    }
                }

            }

            //Удаление ссылки
            if (lineList[row - 3] == "Link") {
                var numberTaskBefore = 0
                for (i in 0..(row - 3)) {
                    if (lineList[i] == "To do list" || lineList[i] == "Image")
                        numberTaskBefore++
                }

                content.removeAt(row / 3 - numberTaskBefore - 1)
                File("src/texts.txt").bufferedWriter().use { out ->
                    for (i in 0 until content.size) {
                        out.write(content[i])
                        out.write("\n")
                    }
                }
            }
            lineList.removeAt(row - 1)
            lineList.removeAt(row - 2)
            lineList.removeAt(row - 3)

            //Запись в файл
            File("src/readTable.txt").bufferedWriter().use { out ->
                for (i in 0 until lineList.size) {
                    out.write(lineList[i])
                    out.write("\n")
                }
            }
            model.removeRow(table.selectedRow)
        }

        //Редактирование
        changeButton.addActionListener {
            val row = table.selectedRow * 3

            //Редактирование Текстовой заметки
            if (lineList[row - 3] == "Text Note") {
                var numberTaskBefore = 0
                for (i in 0..(row - 3)) {
                    if (lineList[i] == "To do list" || lineList[i] == "Image")
                        numberTaskBefore++
                }
                //новое окно для ввода заметки
                val noteWindow = JDialog(this, "Text Note")
                //текстовое поле
                val windowTextNote = JTextArea(content[row / 3 - numberTaskBefore - 1], 200, 250)
                val titleTextNote = JTextField(lineList[row - 2], 250)
                //кнопка сохранения
                val saveButton = JButton("Save")
                saveButton.addActionListener {
                    noteWindow.isVisible = false
                    val dataTime = LocalDate.now()
                    content[row / 3 - numberTaskBefore - 1] = windowTextNote.text
                    lineList[row - 3] = "Text Note"
                    lineList[row - 2] = titleTextNote.text
                    lineList[row - 1] = "$dataTime"
                    //Запись в файл
                    File("src/readTable.txt").bufferedWriter().use { out ->
                        for (i in 0 until lineList.size) {
                            out.write(lineList[i])
                            out.write("\n")
                        }
                    }
                    File("src/texts.txt").bufferedWriter().use { out ->
                        for (i in 0 until content.size) {
                            out.write(content[i])
                            out.write("\n")
                        }
                    }
                    model.setValueAt("Text Note", row / 3, 0)
                    model.setValueAt(titleTextNote.text, row / 3, 1)
                    model.setValueAt("$dataTime", row / 3, 2)
                }
                //табуляция
                windowTextNote.lineWrap = true
                //скроллинг
                val scrollPane = JScrollPane(windowTextNote)
                //расположение элементов
                noteWindow.add(scrollPane, BorderLayout.CENTER)
                noteWindow.add(titleTextNote, BorderLayout.NORTH)
                noteWindow.add(saveButton, BorderLayout.SOUTH)
                noteWindow.setSize(250, 350)
                noteWindow.isVisible = true
                noteWindow.setLocationRelativeTo(null)
            }

            //Редактирование Задачи
            if (lineList[row - 3] == "To do list") {
                var numTask = 0
                for (i in 0..(row - 3)) {
                    if (lineList[i] == "To do list")
                        numTask++
                }
                //новое окно для ввода заметки
                val noteWindow = JDialog(this, "To do list")
                //Для подсчета задач
                var number = 0
                //создание таблицы для задач
                val modelCheckBox: DefaultTableModel
                modelCheckBox = object : DefaultTableModel() {
                    override fun getColumnClass(columnIndex: Int): Class<*> {
                        return when (columnIndex) {
                            0 -> java.lang.Boolean::class.java
                            else -> Any::class.java
                        }
                    }
                }
                val name: Array<Any> = arrayOf(false, "")
                modelCheckBox.setColumnIdentifiers(name)
                val jTable1 = JTable()
                jTable1.model = modelCheckBox

                //Настройка шириины 1 столбца
                val columnModel = jTable1.columnModel.getColumn(0)
                columnModel.maxWidth = 15

                //кнопки добавления и сохранения
                val addButton = JButton("+")
                addButton.addActionListener {
                    number++
                    modelCheckBox.addRow(name)
                }
                //Заполнение из файлов
                var sum = 0
                for (i in 0 until numTask - 1) {
                    sum += 2 * numberTask[i].toInt()
                }
                var sumForFile = sum
                for (i in 0 until numberTask[numTask - 1].toInt()) {
                    modelCheckBox.addRow(name)
                    modelCheckBox.setValueAt(task[sum + i].toBoolean(), i, 0)
                    modelCheckBox.setValueAt(task[sum + i + 1], i, 1)
                    sum++
                }
                val titleTextNote = JTextField(lineList[row - 2], 250)

                val saveButton = JButton("Save")
                saveButton.addActionListener {
                    noteWindow.isVisible = false
                    val dataTime = LocalDate.now()
                    lineList[row - 3] = "To do list"
                    lineList[row - 2] = titleTextNote.text
                    lineList[row - 1] = "$dataTime"
                    for (i in 0 until numberTask[numTask - 1].toInt()) {
                        task[sumForFile + i] = jTable1.getValueAt(i, 0).toString()
                        task[sumForFile + i + 1] = jTable1.getValueAt(i, 1).toString()
                        sumForFile++
                    }
                    var counter = numberTask[numTask - 1].toInt()
                    numberTask[numTask - 1] = (number + numberTask[numTask - 1].toInt()).toString()
                    for (i in sumForFile + 1 until sumForFile + 1 + number) {
                        task.add(i, jTable1.getValueAt(counter, 0).toString())
                        task.add(i + 1, jTable1.getValueAt(counter, 1).toString())
                        counter++
                    }
                    //Запись в файл
                    File("src/readTable.txt").bufferedWriter().use { out ->
                        for (i in 0 until lineList.size) {
                            out.write(lineList[i])
                            out.write("\n")
                        }
                    }
                    File("src/task.txt").bufferedWriter().use { out ->
                        for (i in 0 until task.size) {
                            out.write(task[i])
                            out.write("\n")
                        }
                    }
                    File("src/NumTask.txt").bufferedWriter().use { out ->
                        for (i in 0 until numberTask.size) {
                            out.write(numberTask[i])
                            out.write("\n")
                        }
                    }
                    model.setValueAt("To do list", row / 3, 0)
                    model.setValueAt(titleTextNote.text, row / 3, 1)
                    model.setValueAt("$dataTime", row / 3, 2)
                }

                //добавление кнопок в одну структуру
                val gridCB = JPanel()
                val layCB = GridLayout(1, 0, 5, 12)
                gridCB.layout = layCB
                gridCB.add(saveButton)
                gridCB.add(addButton)

                noteWindow.add(titleTextNote, BorderLayout.NORTH)
                noteWindow.add(gridCB, BorderLayout.SOUTH)
                noteWindow.add(jTable1)
                noteWindow.setSize(250, 350)
                noteWindow.isVisible = true
                noteWindow.setLocationRelativeTo(null)
            }

            //Редактирование ссылки
            if (lineList[row - 3] == "Link") {
                var numberTaskBefore = 0
                for (i in 0..(row - 3)) {
                    if (lineList[i] == "To do list" || lineList[i] == "Image")
                        numberTaskBefore++
                }
                //новое окно для ввода заметки
                val noteWindow = JDialog(this, "Link")
                //текстовое поле
                val windowTextNote = JTextArea(content[row / 3 - numberTaskBefore - 1], 200, 250)
                val titleTextNote = JTextField(lineList[row - 2], 250)
                //кнопка сохранения
                val saveButton = JButton("Save")
                saveButton.addActionListener {
                    noteWindow.isVisible = false
                    val dataTime = LocalDate.now()
                    content[row / 3 - numberTaskBefore - 1] = windowTextNote.text
                    lineList[row - 3] = "Link"
                    lineList[row - 2] = titleTextNote.text
                    lineList[row - 1] = "$dataTime"
                    //Очистка файла
                    FileOutputStream("src/readTable.txt")
                    //Запись в файл
                    File("src/readTable.txt").bufferedWriter().use { out ->
                        for (i in 0 until lineList.size) {
                            out.write(lineList[i])
                            out.write("\n")
                        }
                    }
                    File("src/texts.txt").bufferedWriter().use { out ->
                        for (i in 0 until content.size) {
                            out.write(content[i])
                            out.write("\n")
                        }
                    }
                    model.setValueAt("Link", row / 3, 0)
                    model.setValueAt(titleTextNote.text, row / 3, 1)
                    model.setValueAt("$dataTime", row / 3, 2)
                }
                //табуляция
                windowTextNote.lineWrap = true
                //скроллинг
                val scrollPane = JScrollPane(windowTextNote)
                //расположение элементов
                noteWindow.add(scrollPane, BorderLayout.CENTER)
                noteWindow.add(titleTextNote, BorderLayout.NORTH)
                noteWindow.add(saveButton, BorderLayout.SOUTH)
                noteWindow.setSize(250, 350)
                noteWindow.isVisible = true
                noteWindow.setLocationRelativeTo(null)
            }

            //Редактирование изображения
            if (lineList[row - 3] == "Image") {
                // Создание экземпляра JFileChooser
                val fileChooser = JFileChooser()
                fileChooser.dialogTitle = "Сохранение файла"
                // Определение режима - только файл
                fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
                val result = fileChooser.showSaveDialog(JDialog())
                // Если директория выбрана, покажем ее в сообщении
                if (result == JFileChooser.APPROVE_OPTION) {
                    fileChooser.isVisible = false
                    val dataTime = LocalDate.now()
                    lineList[row - 3] = "Image"
                    lineList[row - 2] = fileChooser.selectedFile.toString()
                    lineList[row - 1] = "$dataTime"
                    //Очистка файла
                    FileOutputStream("src/readTable.txt")
                    //Запись в файл
                    File("src/readTable.txt").bufferedWriter().use { out ->
                        for (i in 0 until lineList.size) {
                            out.write(lineList[i])
                            out.write("\n")
                        }
                    }
                }
            }
        }

        return grid
    }

    //Выбор заметки для создания
    private fun createSwitchWindow() {
        val switchWindow = JDialog(this, "Create Note")
        switchWindow.setSize(250, 250)
        switchWindow.layout = GridLayout(4, 1, 20, 0)
        switchWindow.isResizable = false
        switchWindow.setLocationRelativeTo(null)

        //Текстовая заметка
        val buttonTextNote = JButton("Text Note")
        buttonTextNote.addActionListener {
            switchWindow.isVisible = false
            //новое окно для ввода заметки
            val noteWindow = JDialog(this, "Text Note")
            //текстовое поле
            val windowTextNote = JTextArea(200, 250)
            val titleTextNote = JTextField(250)
            //кнопка сохранения
            val saveButton = JButton("Save")
            saveButton.addActionListener {
                noteWindow.isVisible = false
                val dataTime = LocalDate.now()
                val name: Array<String> = arrayOf("Text Note", titleTextNote.text, "$dataTime")
                model.addRow(name)
                lineList.add("Text Note")
                lineList.add(titleTextNote.text)
                lineList.add("$dataTime")
                content.add(windowTextNote.text)
                //Запись в файл
                File("src/main/kotlin/course_work/readTable.txt").bufferedWriter().use { out ->
                    for (i in 0 until lineList.size) {
                        out.write(lineList[i])
                        out.write("\n")
                    }
                }
                File("src/main/kotlin/course_work/texts.txt").bufferedWriter().use { out ->
                    for (i in 0 until content.size) {
                        out.write(content[i])
                        out.write("\n")
                    }
                }
            }
            //табуляция
            windowTextNote.lineWrap = true
            //скроллинг
            val scrollPane = JScrollPane(windowTextNote)
            //расположение элементов
            noteWindow.add(scrollPane, BorderLayout.CENTER)
            noteWindow.add(titleTextNote, BorderLayout.NORTH)
            noteWindow.add(saveButton, BorderLayout.SOUTH)
            noteWindow.setSize(250, 350)
            noteWindow.isVisible = true
            noteWindow.setLocationRelativeTo(null)

        }
        switchWindow.add(buttonTextNote, BorderLayout.AFTER_LAST_LINE)

        //Задача
        val buttonToDoList = JButton("To do list")
        buttonToDoList.addActionListener {
            switchWindow.isVisible = false
            //новое окно для ввода заметки
            val noteWindow = JDialog(this, "To do list")
            var number = 0
            //создание таблицы для задач
            val modelCheckBox: DefaultTableModel
            modelCheckBox = object : DefaultTableModel() {
                override fun getColumnClass(columnIndex: Int): Class<*> {
                    return when (columnIndex) {
                        0 -> java.lang.Boolean::class.java
                        else -> Any::class.java
                    }
                }
            }

            val name: Array<Any> = arrayOf(false, "")
            modelCheckBox.setColumnIdentifiers(name)
            val jTable1 = JTable()
            jTable1.model = modelCheckBox

            //Настройка шириины 1 столбца
            val columnModel = jTable1.columnModel.getColumn(0)
            columnModel.maxWidth = 15

            //кнопки добавления и сохранения
            val addButton = JButton("+")
            addButton.addActionListener {
                number++
                modelCheckBox.addRow(name)
            }

            val titleTextNote = JTextField(250)
            val saveButton = JButton("Save")
            saveButton.addActionListener {
                noteWindow.isVisible = false
                val dataTime = LocalDate.now()
                val data: Array<String> = arrayOf("To do list", titleTextNote.text, "$dataTime")
                model.addRow(data)
                lineList.add("To do list")
                lineList.add(titleTextNote.text)
                lineList.add("$dataTime")
                numberTask.add(number.toString())

                for (i in 0 until number) {
                    task.add(jTable1.getValueAt(i, 0).toString())
                    task.add(jTable1.getValueAt(i, 1).toString())
                }
                //Запись в файл
                File("src/readTable.txt").bufferedWriter().use { out ->
                    for (i in 0 until lineList.size) {
                        out.write(lineList[i])
                        out.write("\n")
                    }
                }
                File("src/task.txt").bufferedWriter().use { out ->
                    for (i in 0 until task.size) {
                        out.write(task[i])
                        out.write("\n")
                    }
                }
                File("src/NumTask.txt").bufferedWriter().use { out ->
                    for (i in 0 until numberTask.size) {
                        out.write(numberTask[i])
                        out.write("\n")
                    }
                }
            }

            //добавление кнопок в одну структуру
            val grid = JPanel()
            val lay = GridLayout(1, 0, 5, 12)
            grid.layout = lay
            grid.add(saveButton)
            grid.add(addButton)

            noteWindow.add(titleTextNote, BorderLayout.NORTH)
            noteWindow.add(grid, BorderLayout.SOUTH)
            noteWindow.add(jTable1)
            noteWindow.setSize(250, 350)
            noteWindow.isVisible = true
            noteWindow.setLocationRelativeTo(null)

        }
        switchWindow.add(buttonToDoList)

        //Ссылка
        val buttonLink = JButton("Link")
        buttonLink.addActionListener {
            switchWindow.isVisible = false
            //новое окно для ввода заметки
            val noteWindow = JDialog(this, "Link")
            //текстовое поле
            val windowTextNote = JTextArea(200, 250)
            val titleTextNote = JTextField(250)
            //кнопка сохранения
            val saveButton = JButton("Save")
            saveButton.addActionListener {
                noteWindow.isVisible = false
                val dataTime = LocalDate.now()
                val name: Array<String> = arrayOf("Link", titleTextNote.text, "$dataTime")
                model.addRow(name)
                lineList.add("Link")
                lineList.add(titleTextNote.text)
                lineList.add("$dataTime")
                content.add(windowTextNote.text)
                //Запись в файл
                File("src/readTable.txt").bufferedWriter().use { out ->
                    for (i in 0 until lineList.size) {
                        out.write(lineList[i])
                        out.write("\n")
                    }
                }
                File("src/texts.txt").bufferedWriter().use { out ->
                    for (i in 0 until content.size) {
                        out.write(content[i])
                        out.write("\n")
                    }
                }
            }
            //табуляция
            windowTextNote.lineWrap = true
            //скроллинг
            val scrollPane = JScrollPane(windowTextNote)
            noteWindow.add(scrollPane, BorderLayout.CENTER)
            noteWindow.add(titleTextNote, BorderLayout.NORTH)
            noteWindow.add(saveButton, BorderLayout.SOUTH)
            noteWindow.setSize(250, 350)
            noteWindow.isVisible = true
            noteWindow.setLocationRelativeTo(null)

        }
        switchWindow.add(buttonLink)

        //Картинка
        val buttonImage = JButton("Image")
        buttonImage.addActionListener {
            switchWindow.isVisible = false
            // Создание экземпляра JFileChooser
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Сохранение файла"
            // Определение режима - только файл
            fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
            val result = fileChooser.showSaveDialog(JDialog())
            // Если директория выбрана, покажем ее в сообщении
            if (result == JFileChooser.APPROVE_OPTION) {
                fileChooser.isVisible = false
                val dataTime = LocalDate.now()
                val data: Array<String> = arrayOf("Image", fileChooser.selectedFile.toString(), "$dataTime")
                model.addRow(data)
                lineList.add("Image")
                lineList.add(fileChooser.selectedFile.toString())
                lineList.add("$dataTime")
                //Запись в файл
                File("src/readTable.txt").bufferedWriter().use { out ->
                    for (i in 0 until lineList.size) {
                        out.write(lineList[i])
                        out.write("\n")
                    }
                }
            }

        }

        switchWindow.add(buttonImage)
        switchWindow.isFocusable = true
        switchWindow.isVisible = true
    }
}