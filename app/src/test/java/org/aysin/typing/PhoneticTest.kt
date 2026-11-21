package org.aysin.typing

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneticTest {
    @Test fun commonWords() {
        assertEquals("আমি",Phonetic.convert("ami"))
        assertEquals("বাংলা",Phonetic.convert("bangla"))
        assertEquals("তুমি",Phonetic.convert("tumi"))
    }
    @Test fun consonantClusters() {
        assertEquals("ক্র",Phonetic.convert("kr"))
        assertEquals("থ",Phonetic.convert("th"))
        assertEquals("ট",Phonetic.convert("T"))
    }
    @Test fun literalsAndIndependentVowels() {
        assertEquals("",Phonetic.convert(""))
        assertEquals("ই অ!",Phonetic.convert("i o!"))
    }
}
