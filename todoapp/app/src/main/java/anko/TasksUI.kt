package anko

import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableList
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
import kotlin.reflect.KMutableProperty0

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

    private fun <T : Any, U : T> bind(setter: (T) -> Unit, field: ObservableField<U>) {
        field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                setter(field.get())
            }
        })
    }

    private fun bind(setter: (Boolean) -> Unit, field: ObservableBoolean) {
        field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                setter(field.get())
            }
        })
    }

    private fun <T : Any> bind(setter: (T) -> Unit, field: ObservableBoolean, converter: (Boolean) -> T) {
        field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                setter(converter(field.get()))
            }
        })
    }

    private fun <T : Any, U : T> bind(prop: KMutableProperty0<T?>, field: ObservableField<U>) {
        field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                prop.set(field.get())
            }
        })
    }

    private fun <T : Any> bind(setter: (List<T>) -> Unit, field: ObservableList<T>) {
        field.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
            override fun onChanged(p0: ObservableList<T>?) {
                setter(field)
            }

            override fun onItemRangeMoved(p0: ObservableList<T>?, p1: Int, p2: Int, p3: Int) {
                setter(field)
            }

            override fun onItemRangeChanged(p0: ObservableList<T>?, p1: Int, p2: Int) {
                setter(field)
            }

            override fun onItemRangeInserted(p0: ObservableList<T>?, p1: Int, p2: Int) {
                setter(field)
            }

            override fun onItemRangeRemoved(p0: ObservableList<T>?, p1: Int, p2: Int) {
                setter(field)
            }
        })
    }
}
