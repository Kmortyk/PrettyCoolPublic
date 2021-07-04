package com.kmortyk.game.sound

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import java.util.concurrent.CopyOnWriteArrayList

private data class MusicObject(var instances: CopyOnWriteArrayList<Music>)

object SoundMaster {
    const val maxSameSoundInstances = 3

    private val sounds: MutableMap<String, MusicObject> = mutableMapOf()
    private var curMusic: Music? = null

    fun playMusic(name: String) {
        stopMusic()

        curMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/music_$name.mp3"))
        curMusic!!.apply {
            isLooping = true
            play()
        }
    }

    fun stopMusic() {
        if(curMusic != null) {
            curMusic!!.stop()
            curMusic = null
        }
    }

    fun playSound(name: String, loop: Boolean = false) {
        if(name !in sounds) {
            sounds[name] = MusicObject(CopyOnWriteArrayList())
        }

        val o = sounds[name]!!

        val toRem = mutableListOf<Music>()
        for(inst in o.instances) {
            if(!inst.isPlaying) {
                toRem.add(inst)
            }
        }
        o.instances.removeAll(toRem)

        if(o.instances.size >= maxSameSoundInstances)
            return

        val music = Gdx.audio.newMusic(Gdx.files.internal("sounds/sound_$name.mp3"))
        music.isLooping = loop
        music.play()

        o.instances.add(music)
    }
}