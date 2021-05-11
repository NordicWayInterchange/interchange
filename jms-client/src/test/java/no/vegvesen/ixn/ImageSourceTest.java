package no.vegvesen.ixn;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageSourceTest {

    @Test
    public void convertImageToBytes() throws IOException {
        byte[] byteArray = ImageSource.convertImageToByteArray("src/images/cabin_view.jpg");
        assertThat(byteArray[0]).isInstanceOf(Byte.class);
    }
}
