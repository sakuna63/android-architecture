package anko

import android.databinding.ObservableField
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskViewModel
import com.example.android.architecture.blueprints.todoapp.scrollChildSwipeRefreshLayout
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk15.coroutines.textChangedListener
import org.jetbrains.anko.textAppearance
import org.jetbrains.anko.topPadding

/**
 * Generate with Plugin
 * @plugin Kotlin Anko Converter For Xml
 * @version 1.2.1
 */
class AddTaskUI(private val viewModel: AddEditTaskViewModel) : AnkoComponent<AddEditTaskFragment> {

    override fun createView(ui: AnkoContext<AddEditTaskFragment>) = with(ui) {
        scrollChildSwipeRefreshLayout {
            id = R.id.refresh_layout
            bind(::setEnabled, viewModel.dataLoading)
            bind(::setRefreshing, viewModel.dataLoading)
            scrollView {
                linearLayout {
                    orientation = LinearLayout.VERTICAL
                    bottomPadding = dimen(R.dimen.activity_vertical_margin)
                    leftPadding = dimen(R.dimen.activity_horizontal_margin)
                    rightPadding = dimen(R.dimen.activity_horizontal_margin)
                    topPadding = dimen(R.dimen.activity_vertical_margin)
                    bind(::setVisibility, viewModel.dataLoading) { if (it) View.GONE else View.VISIBLE }
                    editText {
                        id = R.id.add_task_title
                        hint = resources.getString(R.string.title_hint)
                        maxLines = 1
                        textAppearance = R.style.TextAppearance_AppCompat_Title
                        bindText(viewModel.title)
                    }.lparams(width = matchParent)
                    editText {
                        id = R.id.add_task_description
                        gravity = Gravity.TOP
                        hint = resources.getString(R.string.description_hint)
                        bindText(viewModel.description)
                    }.lparams(width = matchParent, height = dip(350))
                }.lparams(width = matchParent)
            }.lparams(width = matchParent, height = matchParent)
        }
    }
}

fun TextView.bindText(field: ObservableField<String>) {
    bind(::setText, field)
    textChangedListener {
        afterTextChanged {
            if (field.get() == it.toString()) {
                return@afterTextChanged
            }
            field.set(it.toString())
        }
    }
}



