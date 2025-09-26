package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var btnAddNote: ImageButton
    private lateinit var rvNotes: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var sharedPref: SharedPreferences
    private val gson = Gson()

    private val allNotes = mutableListOf<Note>()
    private val displayedNotes = mutableListOf<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        sharedPref = requireActivity().getSharedPreferences("notes_pref", Context.MODE_PRIVATE)

        setupRecyclerView()
        loadNotes()
        setupSearch()

        btnAddNote.setOnClickListener {
            showNoteDialog(null)
        }
    }

    private fun initViews(view: View) {
        etSearch = view.findViewById(R.id.et_search)
        btnAddNote = view.findViewById(R.id.btn_add_note)
        rvNotes = view.findViewById(R.id.rv_notes)
        tvEmpty = view.findViewById(R.id.tv_empty)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            displayedNotes,
            onNoteClick = { note -> showNoteDialog(note) },
            onNoteLongClick = { note -> showDeleteDialog(note) }
        )

        rvNotes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteAdapter
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterNotes(s.toString())
            }
        })
    }

    private fun filterNotes(query: String) {
        displayedNotes.clear()

        if (query.isEmpty()) {
            displayedNotes.addAll(allNotes)
        } else {
            val filtered = allNotes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
            displayedNotes.addAll(filtered)
        }

        noteAdapter.notifyDataSetChanged()
        updateEmptyView()
    }

    private fun showNoteDialog(note: Note?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_note, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.et_dialog_title)
        val etContent = dialogView.findViewById<EditText>(R.id.et_dialog_content)

        note?.let {
            etTitle.setText(it.title)
            etContent.setText(it.content)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (note == null) "새 노트" else "노트 수정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val title = etTitle.text.toString()
                val content = etContent.text.toString()

                if (note == null) {
                    addNote(Note(title = title, content = content))
                } else {
                    updateNote(note.apply {
                        this.title = title
                        this.content = content
                        this.modifiedDate = System.currentTimeMillis()
                    })
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("노트 삭제")
            .setMessage("이 노트를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteNote(note)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun addNote(note: Note) {
        allNotes.add(0, note)
        saveNotes()
        filterNotes(etSearch.text.toString())
    }

    private fun updateNote(note: Note) {
        val index = allNotes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            allNotes[index] = note
            saveNotes()
            filterNotes(etSearch.text.toString())
        }
    }

    private fun deleteNote(note: Note) {
        allNotes.removeAll { it.id == note.id }
        saveNotes()
        filterNotes(etSearch.text.toString())
    }

    private fun saveNotes() {
        val json = gson.toJson(allNotes)
        sharedPref.edit().putString("notes_list", json).apply()
    }

    private fun loadNotes() {
        val json = sharedPref.getString("notes_list", null)
        if (json != null) {
            val type = object : TypeToken<List<Note>>() {}.type
            val notes: List<Note> = gson.fromJson(json, type)
            allNotes.clear()
            allNotes.addAll(notes)
        }
        filterNotes("")
    }

    private fun updateEmptyView() {
        tvEmpty.visibility = if (displayedNotes.isEmpty()) View.VISIBLE else View.GONE
        rvNotes.visibility = if (displayedNotes.isEmpty()) View.GONE else View.VISIBLE
    }
}