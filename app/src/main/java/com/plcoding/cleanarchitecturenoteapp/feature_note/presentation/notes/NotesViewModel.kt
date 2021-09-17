package com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.notes

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.model.Note
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.use_case.NoteUseCases
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.util.NoteOrder
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases
): ViewModel() {

    private val _state: MutableState<NotesState> = mutableStateOf(NotesState())
    val state: State<NotesState> = _state

    private var recentlyDeletedNote: Note? = null

    private var fetchNotesJob: Job? = null

    init {
        fetchNotes(NoteOrder.default())
    }

    fun onEvent(event: NotesEvent) {
        when(event) {
            is NotesEvent.Order -> handleChangeOrder(event.noteOrder)
            is NotesEvent.DeleteNote -> handleDeleteNote(event.note)
            is NotesEvent.RestoreNote -> handleAddNote(recentlyDeletedNote)
            is NotesEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
        }
    }

    private fun fetchNotes(noteOrder: NoteOrder) {
        fetchNotesJob?.cancel()
        fetchNotesJob = noteUseCases.fetchNotesUseCase(noteOrder)
            .onEach { notes ->
                _state.value = state.value.copy(
                    notes = notes,
                    noteOrder = noteOrder
                )
            }.launchIn(viewModelScope)
    }

    private fun handleDeleteNote(note: Note) = viewModelScope.launch {
        noteUseCases.deleteNoteUseCase(note)
        recentlyDeletedNote = note
    }

    private fun handleAddNote(note: Note?) = viewModelScope.launch {
        noteUseCases.addNoteUseCase(note ?: return@launch)
        recentlyDeletedNote = null
    }

    private fun handleChangeOrder(noteOrder: NoteOrder) {
        if (state.value.noteOrder::class == noteOrder::class && state.value.noteOrder.orderType == noteOrder.orderType)
            return

        fetchNotes(noteOrder)
    }
}