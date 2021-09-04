package com.example.android.architecture.blueprints.todoapp.util;

import com.spotify.dataenum.DataEnum;
import com.spotify.dataenum.dataenum_case;

@DataEnum
public interface Either_dataenum<A, B> {
  dataenum_case Left(A value);

  dataenum_case Right(B value);
}
