package com.example.remindme.ui.reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.remindme.data.LatLng
import com.example.remindme.data.place.PlaceResult
import com.example.remindme.data.place.SearchOutcome
import com.example.remindme.ui.components.ChoiceChipRow
import com.example.remindme.ui.components.ExpandableCard
import com.example.remindme.ui.components.RemindTextField
import com.example.remindme.ui.theme.PreviewSurface
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Typing alone never sets a place, since a reminder can't fire without coordinates. */
@Composable
internal fun LocationCard(
    form: ReminderForm,
    onFormChange: (ReminderForm) -> Unit,
    onSearchPlaces: suspend (String) -> SearchOutcome,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    ExpandableCard(
        icon = Icons.Outlined.LocationOn,
        label = "Location",
        summary = locationSummary(form),
        active = form.hasPlace,
        expanded = expanded,
        onToggle = onToggle
    ) {
        ChoiceChipRow(
            options = PlaceMode.entries,
            selected = form.placeMode,
            onSelect = { onFormChange(form.copy(placeMode = it)) },
            label = { it.label }
        )
        Spacer(Modifier.height(Spacing.md))
        if (form.hasPlace) {
            ChosenPlace(form, onFormChange)
            Spacer(Modifier.height(Spacing.sm))
            val distance = if (form.placeMode == PlaceMode.Near) "Within" else "Farther than"
            CardHint("$distance ${form.radiusMeters} m · change in Account", Modifier.padding(horizontal = Spacing.xs))
        } else {
            PlaceSearchField(
                initialQuery = form.place,
                placeholder = if (form.placeMode == PlaceMode.Near) "Search an address or place" else "Search, like your home address",
                onSearch = onSearchPlaces,
                onPick = { onFormChange(form.withPlace(it)) }
            )
        }
    }
}

@Composable
private fun ChosenPlace(form: ReminderForm, onFormChange: (ReminderForm) -> Unit) {
    RemindTextField(
        value = form.place,
        onValueChange = { onFormChange(form.copy(place = it)) },
        placeholder = "Name it, like Home or Gym",
        leadingIcon = Icons.Outlined.Storefront,
        trailingContent = {
            IconButton(onClick = { onFormChange(form.withoutPlace()) }) {
                Icon(Icons.Outlined.Close, contentDescription = "Remove place")
            }
        }
    )
    if (form.address.isNotBlank()) {
        Spacer(Modifier.height(Spacing.xs))
        CardHint(form.address, Modifier.padding(horizontal = Spacing.xs))
    }
}

@Composable
private fun PlaceSearchField(
    initialQuery: String,
    placeholder: String,
    onSearch: suspend (String) -> SearchOutcome,
    onPick: (PlaceResult) -> Unit,
) {
    val colors = RemindTheme.colors
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var outcome by remember { mutableStateOf<SearchOutcome?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    val searching = job?.isActive == true

    fun search() {
        if (query.isBlank()) return
        keyboard?.hide()
        job?.cancel()
        outcome = null
        job = scope.launch {
            outcome = onSearch(query)
            job = null
        }
    }

    RemindTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = placeholder,
        leadingIcon = Icons.Outlined.Search,
        imeAction = ImeAction.Search,
        onImeAction = ::search,
        trailingContent = {
            if (searching) {
                CircularProgressIndicator(Modifier.size(20.dp), color = colors.textBrand, strokeWidth = 2.dp)
            } else {
                IconButton(onClick = ::search, enabled = query.isNotBlank()) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
            }
        }
    )

    when (val result = outcome) {
        null -> Unit
        SearchOutcome.NotAvailable -> SearchHint("Place search isn't available on this phone.")
        SearchOutcome.Failed -> SearchHint("Couldn't search right now. Check your connection and try again.")
        is SearchOutcome.Found ->
            if (result.places.isEmpty()) SearchHint("No matches. Street addresses work best.")
            else SearchResults(result.places, onPick)
    }
}

@Composable
private fun SearchHint(text: String) {
    Spacer(Modifier.height(Spacing.sm))
    CardHint(text, Modifier.padding(horizontal = Spacing.xs))
}

@Composable
private fun SearchResults(places: List<PlaceResult>, onPick: (PlaceResult) -> Unit) {
    val colors = RemindTheme.colors
    Spacer(Modifier.height(Spacing.xs))
    places.forEach { place ->
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RemindShapes.field)
                .clickable(onClickLabel = "Use this place") { onPick(place) }
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm)
        ) {
            Text(
                place.label,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                place.address,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun locationSummary(form: ReminderForm): String =
    if (!form.hasPlace) "Not set"
    else "${form.placeMode.label} ${form.placeLabel} · ${form.radiusMeters} m"

private val previewPlace = PlaceResult("Green Basket", "Rädisvägen 12, 123 45 Stockholm, Sweden", LatLng(59.33, 18.06))

@PreviewLightDark
@Composable
private fun LocationCardPreview() {
    PreviewSurface {
        LocationCard(
            form = ReminderForm().withPlace(previewPlace),
            onFormChange = {},
            onSearchPlaces = { SearchOutcome.Found(listOf(previewPlace)) },
            expanded = true,
            onToggle = {}
        )
        LocationCard(
            form = ReminderForm(),
            onFormChange = {},
            onSearchPlaces = { SearchOutcome.Found(emptyList()) },
            expanded = true,
            onToggle = {}
        )
        LocationCard(
            form = ReminderForm(),
            onFormChange = {},
            onSearchPlaces = { SearchOutcome.Found(emptyList()) },
            expanded = false,
            onToggle = {}
        )
    }
}
