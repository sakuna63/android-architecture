package anko

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.scrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel
import com.example.android.architecture.blueprints.todoapp.tasks.setItems
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.centerInParent
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.image
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.listView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.textAppearance
import org.jetbrains.anko.textView

/**
 * Generate with Plugin
 * @plugin Kotlin Anko Converter For Xml
 * @version 1.2.1
 */
class TasksUI(private val viewModel: TasksViewModel) : AnkoComponent<TasksFragment> {

    lateinit var tasksList: ListView
    lateinit var refreshLayout: ScrollChildSwipeRefreshLayout

    override fun createView(ui: AnkoContext<TasksFragment>): View = with(ui) {
        scrollChildSwipeRefreshLayout {
            refreshLayout = this
            id = R.id.refresh_layout
            onRefresh { viewModel.loadTasks(true) }
            bind(::setRefreshing, viewModel.dataLoading)

            relativeLayout {
                id = R.id.tasksContainer
                isClickable = true

                linearLayout {
                    id = R.id.tasksLL
                    orientation = LinearLayout.VERTICAL
                    bind(::setVisibility, viewModel.isEmpty) { if (it) View.GONE else View.VISIBLE }
                    textView {
                        id = R.id.filteringLabel
                        textAppearance = R.style.TextAppearance_AppCompat_Title
                        gravity = Gravity.CENTER_VERTICAL
                        bind(::setText, viewModel.currentFilteringLabel)
                    }.lparams(width = matchParent) {
                        leftMargin = dimen(R.dimen.list_item_padding)
                        rightMargin = dimen(R.dimen.list_item_padding)
                        topMargin = dimen(R.dimen.activity_vertical_margin)
                        bottomMargin = dimen(R.dimen.activity_vertical_margin)
                    }
                    tasksList = listView {
                        id = R.id.tasks_list
                        bind(this@listView::setItems, viewModel.items)
                    }.lparams(width = matchParent)
                }.lparams(width = matchParent, height = matchParent)

                linearLayout {
                    id = R.id.noTasks
                    orientation = LinearLayout.VERTICAL
                    bind(::setVisibility, viewModel.isEmpty) { if (it) View.VISIBLE else View.GONE }
                    imageView {
                        id = R.id.noTasksIcon
                        bind(::image, viewModel.noTaskIconRes)
                    }.lparams(width = dip(48), height = dip(48)) {
                        gravity = Gravity.CENTER
                    }
                    textView {
                        id = R.id.noTasksMain
                        bind(::setText, viewModel.noTasksLabel)
                    }.lparams {
                        gravity = Gravity.CENTER
                        bottomMargin = dimen(R.dimen.list_item_padding)
                    }
                    textView {
                        id = R.id.noTasksAdd
                        backgroundResource = R.drawable.touch_feedback
                        gravity = Gravity.CENTER
                        text = resources.getString(R.string.no_tasks_add)
                        onClick { viewModel.addNewTask() }
                        bind(::setVisibility, viewModel.tasksAddViewVisible) { if (it) View.VISIBLE else View.GONE }
                    }.lparams(height = dip(48)) {
                        gravity = Gravity.CENTER
                    }
                }.lparams {
                    centerInParent()
                }
            }.lparams(width = matchParent, height = matchParent)
        }
    }
}
