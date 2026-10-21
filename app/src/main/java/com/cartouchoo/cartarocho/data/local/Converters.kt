package com.cartouchoo.cartarocho.data.local

import androidx.room.TypeConverter
import com.cartouchoo.cartarocho.domain.Camp
import com.cartouchoo.cartarocho.domain.ChelemResult
import com.cartouchoo.cartarocho.domain.Contract
import com.cartouchoo.cartarocho.domain.MisereType
import com.cartouchoo.cartarocho.domain.PetitAuBoutCamp
import com.cartouchoo.cartarocho.domain.PoigneeType

/** Sérialise les enums du domaine sous forme de texte pour le stockage Room. */
class Converters {

    @TypeConverter
    fun contractToString(value: Contract): String = value.name

    @TypeConverter
    fun stringToContract(value: String): Contract = Contract.valueOf(value)

    @TypeConverter
    fun petitAuBoutToString(value: PetitAuBoutCamp): String = value.name

    @TypeConverter
    fun stringToPetitAuBout(value: String): PetitAuBoutCamp = PetitAuBoutCamp.valueOf(value)

    @TypeConverter
    fun poigneeToString(value: PoigneeType): String = value.name

    @TypeConverter
    fun stringToPoignee(value: String): PoigneeType = PoigneeType.valueOf(value)

    @TypeConverter
    fun campToString(value: Camp): String = value.name

    @TypeConverter
    fun stringToCamp(value: String): Camp = Camp.valueOf(value)

    @TypeConverter
    fun chelemToString(value: ChelemResult): String = value.name

    @TypeConverter
    fun stringToChelem(value: String): ChelemResult = ChelemResult.valueOf(value)

    @TypeConverter
    fun misereTypeToString(value: MisereType): String = value.name

    @TypeConverter
    fun stringToMisereType(value: String): MisereType = MisereType.valueOf(value)
}
