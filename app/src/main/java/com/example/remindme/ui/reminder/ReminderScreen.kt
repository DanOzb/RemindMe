package com.example.remindme.ui.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.remindme.data.ReminderDefaults
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remindme.data.TaskEntity
import com.example.remindme.ui.TasksViewModel
import com.example.remindme.ui.permissions.rememberExactAlarmRequest
import com.example.remindme.ui.permissions.rememberLocationRequest
import com.example.remindme.ui.tasksViewModel
import kotlinx.coroutines.flow.map

@Composable
fun ReminderScreen(
    reminderId: Int?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = tasksViewModel(),
) {
    if (reminderId == null) {
        ReminderEditor(existing = null, viewModel, onClose, modifier)
        return
    }

    // null while loading; Loaded(null) when the reminder doesn't exist (or was deleted)
    val loaded by remember(reminderId) { viewModel.observeTask(reminderId).map(::Loaded) }
        .collectAsStateWithLifecycle(initialValue = null)

    when (val state = loaded) {
        null -> Unit // Room answers in milliseconds; not worth a spinner
        else -> if (state.task == null) {
            LaunchedEffect(Unit) { onClose() }
        } else {
            ReminderEditor(existing = state.task, viewModel, onClose, modifier)
        }
    }
}

private data class Loaded(val task: TaskEntity?)

@Composable
private fun ReminderEditor(
    existing: TaskEntity?,
    viewModel: TasksViewModel,
    onClose: () -> Unit,
    modifier: Modifier,
) {
    // The radius isn't picked per reminder: it's the app-wide setting from the Account screen
    val context = LocalContext.current
    val radius by ReminderDefaults.getInstance(context).radiusMeters.collectAsStateWithLifecycle()

    // Filled once from the loaded task; later database updates don't overwrite the user's edits
    var form by rememberSaveable(stateSaver = ReminderFormSaver) {
        mutableStateOf(existing?.toForm() ?: ReminderForm())
    }

    // Picking a place is when location access starts to matter, so ask then
    val askLocation = rememberLocationRequest(onFinished = { viewModel.refreshLocation() })
    // Likewise exact alarms, on saving the first reminder with a time
    val askExactAlarms = rememberExactAlarmRequest()

    ReminderContent(
        form = form.copy(radiusMeters = radius),
        onFormChange = {
            if (it.hasPlace && !form.hasPlace) askLocation(false)
            form = it
        },
        onSearchPlaces = viewModel::searchPlaces,
        isEdit = existing != null,
        onSave = {
            viewModel.save(form.copy(radiusMeters = radius).toTask(existing))
            if (form.hasWhen || form.repeatRule != null) askExactAlarms(onClose) else onClose()
        },
        onDelete = existing?.let { task ->
            {
                viewModel.delete(task.id)
                onClose()
            }
        },
        onClose = onClose,
        modifier = modifier
    )
}
