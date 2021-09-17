package com.plcoding.cleanarchitecturenoteapp.feature_note.domain.use_case

data class NoteUseCases(
    val fetchNotesUseCase: FetchNotesUseCase,
    val deleteNoteUseCase: DeleteNoteUseCase,
    val addNoteUseCase: AddNoteUseCase,
    val fetchNoteUseCase: FetchNoteUseCase
)