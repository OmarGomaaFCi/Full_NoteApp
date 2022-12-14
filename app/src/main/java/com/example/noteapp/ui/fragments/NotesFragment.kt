package com.example.noteapp.ui.fragments

import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.amrdeveloper.lottiedialog.LottieDialog
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentNotesBinding
import com.example.noteapp.models.Note
import com.example.noteapp.ui.adapters.NoteAdapter
import com.example.noteapp.ui.adapters.OnItemClickListener
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.ui.viewmodel.NoteViewModelFactory

class NotesFragment : Fragment() {


    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteViewModelFactory: NoteViewModelFactory

    private var layout: RecyclerViewLayout = RecyclerViewLayout.LINEAR
    private var currentData: List<Note>? = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notes, container, false)
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        noteViewModelFactory = NoteViewModelFactory(requireActivity().application)
        noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]
        requireActivity().setTheme(R.style.Theme_NoteApp)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = binding.fabAddNote
        val gridButton = view.findViewById<ImageView>(R.id.gridView)
        val linearView = view.findViewById<ImageView>(R.id.linearView)

        noteViewModel.getAllNotes().observe(viewLifecycleOwner, Observer { notes ->
            if (notes.isEmpty()) {
                binding.lottieNoNotes.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                currentData = notes
                setRecyclerView(notes)
            }

        })

        fab.setOnClickListener {
            val action = NotesFragmentDirections.actionNotesFragmentToAddNoteFragment()
            findNavController().navigate(action)
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    fab.collapse()
                } else {
                    fab.expand()
                }
            }
        })

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteViewModel.searchNote(text.toString()).observe(viewLifecycleOwner) { notes ->
                    if (notes.isEmpty()) {
                        binding.lottieNoNotes.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.lottieNoNotes.visibility = View.GONE
                        binding.lottieNoNotes.visibility = View.VISIBLE
                        currentData = notes
                        setRecyclerView(notes)
                    }

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        gridButton.setOnClickListener {
            layout = RecyclerViewLayout.GRID
            (it as ImageView).setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.purple_500),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            linearView.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.grey),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            setRecyclerView()
        }

        linearView.setOnClickListener {
            layout = RecyclerViewLayout.LINEAR
            gridButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.grey),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            (it as ImageView).setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.purple_500),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            setRecyclerView()
        }
    }

    private fun setRecyclerView(notes: List<Note> = currentData!!) {
        if (notes.isNotEmpty()) {
            binding.lottieNoNotes.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE

            val noteAdapter = NoteAdapter(notes.toMutableList(), object : OnItemClickListener {
                override fun onItemClick(note: Note) {
                    val action =
                        NotesFragmentDirections.actionNotesFragmentToSaveOrDeleteFragment(note)
                    findNavController().navigate(action)
                }

                override fun onLongItemClick(note: Note, view: View) {
                    popUpMenu(note, view)
                }
            })
            binding.recyclerView.apply {
                adapter = noteAdapter
                layoutManager =
                    if (layout == RecyclerViewLayout.LINEAR) LinearLayoutManager(context)
                    else StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            }
        } else {
            binding.lottieNoNotes.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }

    }

    private fun popUpMenu(note: Note, view: View) {
        val popup = PopupMenu(context, view)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popup_delete -> {
                    noteViewModel.deleteNote(note)
                    val lottieDialogue = LottieDialog(context)
                    lottieDialogue.apply {
                        setAnimation(R.raw.deleted_successfully)
                        setAnimationRepeatCount(0)
                        setAutoPlayAnimation(true)
                        setMessage("Deleted Successfully !!")

                    }.show()
                    noteViewModel.getAllNotes().observeForever { list ->
                        currentData = list
                    }
                }

            }
            false
        }
        popup.inflate(R.menu.popup_menu)
        popup.show()
    }

    enum class RecyclerViewLayout {
        LINEAR,
        GRID,
    }

}