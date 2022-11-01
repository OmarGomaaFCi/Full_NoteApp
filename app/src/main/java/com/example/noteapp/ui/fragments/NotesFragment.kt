package com.example.noteapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notes, container, false)
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        noteViewModelFactory = NoteViewModelFactory(requireActivity().application)
        noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = binding.fabAddNote

        noteViewModel.getAllNotes().observe(viewLifecycleOwner, Observer { notes ->
            if (notes.isEmpty()) {
                binding.imageViewNoData.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                setRecyclerView(notes)
            }

        })

        fab.setOnClickListener {
            val action = NotesFragmentDirections.actionNotesFragmentToAddNoteFragment()
            findNavController().navigate(action)
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0){
                    fab.collapse()
                }else {
                    fab.expand()
                }
            }
        })
    }

    private fun setRecyclerView(notes: List<Note>) {
        binding.imageViewNoData.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE

        val noteAdapter = NoteAdapter(notes.toMutableList() , object : OnItemClickListener{
            override fun onItemClick(note: Note) {
                val action = NotesFragmentDirections.actionNotesFragmentToSaveOrDeleteFragment(note)
                findNavController().navigate(action)
            }
        })
        binding.recyclerView.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

}