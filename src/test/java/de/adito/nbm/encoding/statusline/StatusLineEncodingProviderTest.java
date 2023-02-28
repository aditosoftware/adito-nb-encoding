package de.adito.nbm.encoding.statusline;

import de.adito.notification.internal.NotificationFacadeTestUtil;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

/**
 * Test class for {@link StatusLineEncodingProvider}.
 *
 * @author r.hartinger, 23.02.2023
 */
class StatusLineEncodingProviderTest
{

  /**
   * Tests the method {@link StatusLineEncodingProvider#setEncoding(String)}.
   */
  @Nested
  class SetEncoding
  {
    /**
     * Tests that an IOException will be logged, if an invalid encoding will be passed to the method.
     */
    @Test
    @SneakyThrows
    void shouldNotifyAboutIOException()
    {
      String notExistingEncoding = "NotExistingEncoding";
      assertThrows(UnsupportedCharsetException.class, () -> Charset.forName(notExistingEncoding), "encoding with the name " + notExistingEncoding + " should not exist");

      NotificationFacadeTestUtil.verifyNotificationFacade(IOException.class, () -> baseSetEncoding(notExistingEncoding));
    }


    /**
     * Tests that nothing will be logged, if an existing encoding will be passed to the method.
     */
    @Test
    @SneakyThrows
    void shouldSetEncoding()
    {
      NotificationFacadeTestUtil.verifyNoInteractionsWithNotificationFacade(() -> baseSetEncoding(StandardCharsets.UTF_8.name()));
    }

    /**
     * Base method for testing {@link StatusLineEncodingProvider#setEncoding(String)}.
     * The static mocks are used for creating an instance of the class via the constructor.
     *
     * @param pEncoding the encoding that should be passed to the method
     */
    @SneakyThrows
    private void baseSetEncoding(@NotNull String pEncoding)
    {
      OutputStream outputStream = Mockito.mock(OutputStream.class);

      FileObject fileObject = Mockito.mock(FileObject.class);
      Mockito.doReturn(new byte[0]).when(fileObject).asBytes();
      Mockito.doReturn(outputStream).when(fileObject).getOutputStream();

      try (MockedStatic<FileEncodingQuery> fileEncodingQueryMockedStatic = Mockito.mockStatic(FileEncodingQuery.class))
      {

        fileEncodingQueryMockedStatic.when(() -> FileEncodingQuery.getEncoding(any())).thenReturn(StandardCharsets.UTF_8);

        StatusLineEncodingProvider statusLineEncodingProvider = Mockito.mock(StatusLineEncodingProvider.class);
        Mockito.doReturn(fileObject).when(statusLineEncodingProvider).getFileObject();
        Mockito.doCallRealMethod().when(statusLineEncodingProvider).setEncoding(any());

        Field field = statusLineEncodingProvider.getClass().getDeclaredField("encodingList");
        field.setAccessible(true);

        field.set(statusLineEncodingProvider, new JList<>());

        statusLineEncodingProvider.setEncoding(pEncoding);
      }
    }
  }

}
