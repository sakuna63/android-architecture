package anko

import android.databinding.Observable
import android.databinding.ObservableField
import android.databinding.ObservableList
import org.jetbrains.anko.AnkoComponent
import java.beans.PropertyChangeListener
import kotlin.reflect.KMutableProperty0

abstract class BindableAnkoComponent<in T> : AnkoComponent<T> {
    val observers = mutableMapOf<Int, List<PropertyChangeListener>>()
}

fun <T : Any, U : T> bind(setter: (T) -> Unit, field: ObservableField<U>) {
    setter(field.get())
    field.addOnPropertyChangedCallback { setter(it) }
}

fun <T : Any, U : Any> bind(setter: (T) -> Unit, field: ObservableField<U>, converter: (U) -> T) {
    setter(converter(field.get()))
    field.addOnPropertyChangedCallback { setter(converter(it)) }
}

fun <T : Any, U : T> bind(prop: KMutableProperty0<T?>, field: ObservableField<U>) {
    prop.set(field.get())
    field.addOnPropertyChangedCallback { prop.set(it) }
}

private fun <T : Any> ObservableField<T>.addOnPropertyChangedCallback(block: (newValue: T) -> Unit) {
    addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(p0: Observable?, p1: Int) {
            block(get())
        }
    })
}

fun <T : Any> bind(setter: (List<T>) -> Unit, field: ObservableList<T>) {
    setter(field)
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
