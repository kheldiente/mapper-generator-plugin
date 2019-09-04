package ui

import com.intellij.ide.util.TreeFileChooser
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.io.FileUtil
import generator.KotlinMappingClassGenerator
import generator.MappingClassConfig
import extractor.data.ClassTree
import utils.StringsBundle
import javax.swing.JComponent

class GenerateMappingClassDialog constructor(private val project: Project,
                                             private val absolutePath: String): DialogWrapper(true), GenerateMappingClassPanel.EventListener {

    private val presenter: GenerateMappingClassPresenter = GenerateMappingClassPresenter(this, project)
    private val panel = GenerateMappingClassPanel()

    init {
        panel.listener = this
        init()
    }

    override fun createCenterPanel(): JComponent? = panel

    override fun doOKAction() {
        super.doOKAction()

        val generator = KotlinMappingClassGenerator()
        val config = MappingClassConfig(
            hasBaseClass = true,
            baseClassName = panel.baseClassName,
            className = panel.className,
            path = absolutePath
        )

        generator.execute(config)
    }

    override fun onSelectFirstClass() {
        val classTree = selectAFile()
        panel.firstClassTree = classTree
        println("onSelectFirstClass: $classTree")
    }

    override fun onSelectSecondClass() {
        val classTree = selectAFile()
        panel.secondClassTree = classTree
        println("onSelectSecondClass: $classTree")
    }

    private fun selectAFile(): ClassTree {
        val kotlinType = FileTypeManager.getInstance().getFileTypeByExtension("kt")
        val kotlinFileFilter = TreeFileChooser.PsiFileFilter { file -> file.fileType.defaultExtension.equals("kt", ignoreCase = true) }

        val classChooser = TreeFileChooserFactory.getInstance(project)
            .createFileChooser(
                StringsBundle.message("select.class"),
                null,
                kotlinType,
                kotlinFileFilter
            )
        classChooser.showDialog()

        val selectedFiled = classChooser.selectedFile
        val virtualFile = selectedFiled?.virtualFile
        var classTree = ClassTree()
        virtualFile?.let {
            val path = FileUtil.toSystemDependentName(virtualFile.path)
            val name = virtualFile.name
            println("file name: $name, path: $path")

           classTree = presenter.getClassTree(virtualFile)
        }
        return classTree
    }

}