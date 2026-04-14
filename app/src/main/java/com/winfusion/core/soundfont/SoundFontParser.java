/*
 * Soundfont 2 File Parser
 * based in small parts on WaveAudioFileReader.java of the tritonus
 * project: www.tritonus.org
 */

/*
 *  Copyright (c) 1999,2000 by Florian Bomers
 *  Copyright (c) 1999 by Matthias Pfisterer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Junyu Long on 2025
 * - Retained only the parts related to SoundFontInfo.
 */

package com.winfusion.core.soundfont;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

/**
 * Class to parse a SoundFont 2 file.
 *
 * @author florian
 */
public class SoundFontParser {

    public static final String TAG = "SoundfontParser";

    public static final int FOURCC_RIFF = 0x52494646;
    public static final int FOURCC_sfbk = 0x7366626B;
    public static final int FOURCC_LIST = 0x4C495354;
    public static final int FOURCC_INFO = 0x494E464F;
    public static final int FOURCC_sdta = 0x73647461;
    public static final int FOURCC_pdta = 0x70647461;
    public static final int FOURCC_ifil = 0x6966696C;
    public static final int FOURCC_isng = 0x69736E67;
    public static final int FOURCC_INAM = 0x494E414D;
    public static final int FOURCC_irom = 0x69726F6D;
    public static final int FOURCC_iver = 0x69766572;
    public static final int FOURCC_ICRD = 0x49435244;
    public static final int FOURCC_IENG = 0x49454E47;
    public static final int FOURCC_IPRD = 0x49505244;
    public static final int FOURCC_ICOP = 0x49434F50;
    public static final int FOURCC_ICMT = 0x49434D54;
    public static final int FOURCC_ISFT = 0x49534654;

    // special, internal FOURCC's:
    public static final int FOURCC_OUTERCHUNK = 0x00000000;
    public static final int FOURCC_IGNORED = 0x00000001;

    /**
     * The stream to read from. It should be buffered and should not be accessed
     * directly -- only through this classes' readXXX methods.
     */
    private InputStream inputStream;

    /**
     * The current read position in dis.
     */
    private long readPos = 0;

    /**
     * Meta data found in the soundfont
     */
    private SoundFontInfo infoData = null;

    /**
     * Create a SoundFont 2 parser
     */
    public SoundFontParser() {

    }

    /**
     * @return Returns the SoundFont meta data (only valid after load).
     */
    @Nullable
    public SoundFontInfo getInfo() {
        return infoData;
    }

    /**
     * Actually read the soundbank from the stream and parse it into the
     * infoData, sampleData, and presetData fields.
     *
     * @param in the input stream to read from -- preferably a buffered stream.
     * @throws IOException               on stream read error or premature end of stream
     * @throws SoundFont2ParserException if the stream is not a well-structured
     *                                   SoundFont 2 file.
     */
    public void load(@NonNull InputStream in) throws IOException,
            SoundFont2ParserException {
        infoData = null;
        this.inputStream = in;
        readChunks(0xFFFFFFFFFFFFFFFL, FOURCC_OUTERCHUNK);
        in.close();
    }

    /**
     * Read a little endian 32-bit int from the stream. Advance readPos by 4.
     *
     * @return the 32-bit integer value
     */
    public int readIntLE() throws IOException {
        assert (scheduledSkip == 0);
        int b0 = inputStream.read();
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        if ((b0 | b1 | b2 | b3) < 0) {
            throw new EOFException();
        }
        readPos += 4;
        return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
    }

    /**
     * Read a signed little endian 16-bit short from the stream. Advance readPos
     * by 2.
     *
     * @return the 16-bit short value
     */
    public short readShortLE() throws IOException {
        assert (scheduledSkip == 0);
        int b0 = inputStream.read();
        int b1 = inputStream.read();
        if ((b0 | b1) < 0) {
            throw new EOFException();
        }
        readPos += 2;
        return (short) ((b1 << 8) + b0);
    }

    /**
     * Read a big endian 32-bit int from the stream. Advance readPos by 4.
     *
     * @return the 32-bit integer value
     */
    public int readIntBE() throws IOException {
        assert (scheduledSkip == 0);
        int b3 = inputStream.read();
        int b2 = inputStream.read();
        int b1 = inputStream.read();
        int b0 = inputStream.read();
        if ((b0 | b1 | b2 | b3) < 0) {
            throw new EOFException();
        }
        readPos += 4;
        return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
    }

    public void readFully(@NonNull byte[] bytes) throws IOException {
        readFully(bytes, 0, bytes.length);
    }

    public void readFully(@NonNull byte[] bytes, int offset, int length)
            throws IOException {
        readPos += length;
        while (length > 0) {
            int read = inputStream.read(bytes, offset, length);
            if (read > 0) {
                length -= read;
                offset += read;
            } else if (read == 0) {
                Thread.yield();
            } else {
                throw new EOFException();
            }
        }
    }

    public String readString(long chunkLength) throws IOException,
            SoundFont2ParserException {
        // sanity
        if (chunkLength > 100000) {
            throw new SoundFont2ParserException(
                    "corrupt soundfont: string subchunk>100000 bytes");
        }
        byte[] bytes = new byte[(int) chunkLength];
        readFully(bytes);
        // find terminator
        while (chunkLength > 0 && bytes[(int) chunkLength - 1] == 0) {
            chunkLength--;
        }
        return new String(bytes, 0, (int) chunkLength);
    }

    private long scheduledSkip = 0;

    protected void scheduleAdvanceChunk(long chunkStart, long chunkLength) {
        long chunkRead = readPos - chunkStart;
        if (chunkLength > 0) {
            long add = ((chunkLength + 1) & 0xFFFFFFFEL) - chunkRead;
            scheduledSkip += add;
            readPos += add;
        }
    }

    protected void commitAdvanceChunk() throws IOException {
        if (scheduledSkip > 0) {
            skip(scheduledSkip);
            // compensate readPos (because it was already increased in
            // scheduleAdvanceChunk)
            readPos -= scheduledSkip;
        }
        scheduledSkip = 0;
    }

    protected void skip(long bytes) throws IOException {
        while (bytes > 0) {
            long skipped = inputStream.skip(bytes);
            if (skipped > 0) {
                bytes -= skipped;
                readPos += skipped;
            }
        }
    }

    protected String key2string(int key) {
        if (key == FOURCC_IGNORED) {
            return "IGNORED_LIST";
        } else if (key == FOURCC_OUTERCHUNK) {
            return "OUTER";
        }
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (key & 0xFF);
        bytes[2] = (byte) ((key & 0xFF00) >> 8);
        bytes[1] = (byte) ((key & 0xFF0000) >> 16);
        bytes[0] = (byte) ((key & 0xFF000000) >> 24);
        return new String(bytes);
    }

    private void requireChunk(int chunkID, int fourcc)
            throws SoundFont2ParserException {
        if (chunkID != fourcc) {
            throw new SoundFont2ParserException(
                    "corrupt soundfont file: found chunk "
                            + key2string(chunkID) + " but expected "
                            + key2string(fourcc));
        }
    }

    private void readChunks(long outerChunkLength, int listChunk)
            throws SoundFont2ParserException, IOException {

        long endPos = readPos + outerChunkLength;

        while (readPos < endPos) {

            // commit skipped bytes
            commitAdvanceChunk();
            long chunkLength;
            int thisChunk;

            try {
                thisChunk = readIntBE();
                chunkLength = readIntLE(); // unsigned
            } catch (IOException e) {
                // when we come here, we skipped past the end of the file
                throw new SoundFont2ParserException(
                        "corrupt soundfont: premature end of file.", e);
            }

            long chunkStart = readPos;

            // depending on level, parse and read the different chunk types
            switch (listChunk) {
                case FOURCC_OUTERCHUNK:
                    // special list type: for the outer level
                    requireChunk(thisChunk, FOURCC_RIFF);
                    int header = readIntBE();
                    requireChunk(header, FOURCC_sfbk);
                    readChunks(chunkLength - 4, header);
                    break;

                case FOURCC_sfbk:
                    // soundfonts only have LIST chunks on 1st level
                    requireChunk(thisChunk, FOURCC_LIST);
                    int listType = readIntBE();
                    switch (listType) {
                        case FOURCC_INFO:
                            readChunks(chunkLength - 4, listType);
                            if (infoData.getVersionMajor() == 0
                                    && infoData.getVersionMinor() == 0) {
                                infoData.setVersion(2, 0);
                            }
                            break;

                        case FOURCC_sdta, FOURCC_pdta:
                            readChunks(chunkLength - 4, listType);
                            break;

                        default:
                            readChunks(chunkLength - 4, FOURCC_IGNORED);
                            break;
                    }
                    break;

                case FOURCC_INFO:
                    readINFO(thisChunk, chunkLength);
                    break;

                default:
                    break;
            }
            // don't advance immediately:
            // some RIFF files truncate the end of the last chunks.
            // if we'd try to skip to the end of the last chunk, an EOFException
            // would be thrown, although the file is not corrupt.
            scheduleAdvanceChunk(chunkStart, chunkLength);
            if (listChunk == FOURCC_OUTERCHUNK) {
                // the outermost chunk can only have one element
                break;
            }
        }
        // another consistency check
        if (readPos > endPos) {
            throw new SoundFont2ParserException(
                    "corrupt soundfont: inner chunk is declared larger than fits "
                            + "into outer chunk");
        }
    }

    private void checkSize(int chunkID, long chunkLength)
            throws SoundFont2ParserException {

        long expectedMinSize = 4;
        int blockCount = (int) (chunkLength / 4);
        // first check that chunkLength is a multiple of blockSize
        if (((long) 4 * blockCount) != chunkLength) {
            throw new SoundFont2ParserException("corrupt soundfont: chunk "
                    + key2string(chunkID) + "'s length of " + chunkLength
                    + " is not a multiple of the block size " + 4);
        }
        if (chunkLength < expectedMinSize) {
            throw new SoundFont2ParserException("corrupt soundfont: chunk "
                    + key2string(chunkID) + "'s length of " + chunkLength
                    + " is too small to accomodate at least " + 1
                    + " blocks");
        }
    }

    private void readINFO(int chunkID, long chunkLength) throws IOException, SoundFont2ParserException {
        if (infoData == null)
            infoData = new SoundFontInfo();

        switch (chunkID) {
            case FOURCC_ifil:
                checkSize(chunkID, chunkLength);
                infoData.setVersion(readShortLE(), readShortLE());
                break;

            case FOURCC_isng:
                infoData.setSoundEngine(readString(chunkLength));
                break;

            case FOURCC_INAM:
                infoData.setName(readString(chunkLength));
                break;

            case FOURCC_irom:
                infoData.setRomName(readString(chunkLength));
                break;

            case FOURCC_iver:
                checkSize(chunkID, chunkLength);
                infoData.setROMVersion(readShortLE(), readShortLE());
                break;

            case FOURCC_ICRD:
                infoData.setCreationDate(readString(chunkLength));
                break;

            case FOURCC_IENG:
                infoData.setEngineer(readString(chunkLength));
                break;

            case FOURCC_IPRD:
                infoData.setProduct(readString(chunkLength));
                break;

            case FOURCC_ICOP:
                infoData.setCopyright(readString(chunkLength));
                break;

            case FOURCC_ICMT:
                infoData.setComment(readString(chunkLength));
                break;

            case FOURCC_ISFT:
                infoData.setSoftware(readString(chunkLength));
                break;
        }
    }

    public static class SoundFont2ParserException extends Exception {

        @Serial
        private static final long serialVersionUID = 0;

        public SoundFont2ParserException(String msg) {
            super(msg);
        }

        public SoundFont2ParserException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}
