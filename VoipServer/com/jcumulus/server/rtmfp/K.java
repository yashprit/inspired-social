package com.jcumulus.server.rtmfp;


/**
 * jCumulus is a Java port of Cumulus OpenRTMP
 *
 * Copyright 2011 OpenRTMFP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License received along this program for more
 * details (or else see http://www.gnu.org/licenses/).
 *
 *
 * This file is a part of jCumulus.
 */

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;

// Referenced classes of package com.jcumulus.server.rtmfp:
//            C

public class K
{
    static enum _A
    {
		DECRYPT, ENCRYPT
	}

    K(byte abyte0[], _A _pa)
    {
        E = _pa;
        if(abyte0 == null)
        {
            return;
        } else
        {
            A = abyte0.length;
            C = new SecretKeySpec(abyte0, 0, 16, "AES");
            return;
        }
    }

    public boolean A()
    {
        return C != null;
    }

    byte[] A(byte abyte0[], int i, int j)
    {
        if(C == null)
            return abyte0;
        try
        {
            byte abyte1[] = new byte[16];
            IvParameterSpec ivparameterspec = new IvParameterSpec(abyte1);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            if(E == _A.ENCRYPT)
                cipher.init(1, C, ivparameterspec);
            if(E == _A.DECRYPT)
                cipher.init(2, C, ivparameterspec);

            return cipher.doFinal(abyte0, i, j);
        }
        catch(NoSuchAlgorithmException nosuchalgorithmexception)
        {
            D.error(nosuchalgorithmexception.getMessage(), nosuchalgorithmexception);
        }
        catch(NoSuchPaddingException nosuchpaddingexception)
        {
            D.error(nosuchpaddingexception.getMessage(), nosuchpaddingexception);
        }
        catch(InvalidKeyException invalidkeyexception)
        {
            D.error(invalidkeyexception.getMessage(), invalidkeyexception);
        }
        catch(BadPaddingException badpaddingexception)
        {
            D.error(badpaddingexception.getMessage(), badpaddingexception);
        }
        catch(IllegalBlockSizeException illegalblocksizeexception)
        {
            D.error(illegalblocksizeexception.getMessage(), illegalblocksizeexception);
        }
        catch(InvalidAlgorithmParameterException invalidalgorithmparameterexception)
        {
            D.error(invalidalgorithmparameterexception.getMessage(), invalidalgorithmparameterexception);
        }
        return null;
    }

    private static final Logger D = Logger.getLogger(C.class);
    public static final int B = 32;
    _A E;
    SecretKeySpec C;
    int A;

}
