package com.emine.todolistroom.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.emine.todolistroom.adapter.IcerikAdapter
import com.emine.todolistroom.databinding.FragmentGorevlerBinding
import com.emine.todolistroom.model.Icerik
import com.emine.todolistroom.roomdb.IcerikDAO
import com.emine.todolistroom.roomdb.IcerikDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class Gorevler : Fragment() {
    private var _binding: FragmentGorevlerBinding? = null
    private val binding get() = _binding!!

    private val mDisposabla= CompositeDisposable()

    private lateinit var db: IcerikDatabase
    private lateinit var icerikDao: IcerikDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(),IcerikDatabase::class.java, "Gorevler").build()
        icerikDao =db.icerikDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGorevlerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener{yeniEkle(it)}
        binding.gorevRecyclerView.layoutManager= LinearLayoutManager(requireContext())
        verileriAl()

    }

    private fun verileriAl(){
        mDisposabla.add(
            icerikDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }
   private fun handleResponse(icerikler: List<Icerik>){
       val adapter= IcerikAdapter(icerikler)
       binding.gorevRecyclerView.adapter=adapter
   }
    /* private fun handleResponse(icerikler: List<Icerik>){
       icerikler.forEach{
           println(it.gorev)
           println(it.icerik)
       }
   }*/



    fun yeniEkle(view: View){
        val action= GorevlerDirections.Companion.actionGorevlerToGorevlerIcerik(bilgi = "yeni",id=-1)
        Navigation.findNavController(view).navigate(action)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposabla.clear()
    }


}