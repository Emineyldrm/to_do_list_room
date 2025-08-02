package com.emine.todolistroom.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.emine.todolistroom.databinding.RecyclerRowBinding
import com.emine.todolistroom.model.Icerik
import com.emine.todolistroom.view.GorevlerDirections

class IcerikAdapter(val icerikListesi: List<Icerik>): RecyclerView.Adapter<IcerikAdapter.IcerikHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IcerikHolder {
        val RecyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return IcerikHolder(RecyclerRowBinding)
    }

    override fun onBindViewHolder(holder: IcerikHolder,position: Int) {
        holder.binding.recyclerViewTextView.text=icerikListesi[position].gorev
        holder.itemView.setOnClickListener{
            val action= GorevlerDirections.actionGorevlerToGorevlerIcerik(bilgi="eski",id=icerikListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return icerikListesi.size
    }

    class IcerikHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){}
}







