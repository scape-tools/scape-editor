package scape.editor.fs.graphics;

import scape.editor.fs.io.RSBuffer;

public class RSModel {

    private RSModel() {

    }

    public static RSModel decode(byte[] data) {
        RSModel model = new RSModel();

        if (data[data.length - 1] == -1 && data[data.length - 2] == -1) {
            model.decodeNewModel(data);
        } else {
            model.decodeOldModel(data);
        }

        return model;
    }

    public void decodeNewModel(byte data[]) {
        RSBuffer nc1 = RSBuffer.wrap(data);
        RSBuffer nc2 = RSBuffer.wrap(data);
        RSBuffer nc3 = RSBuffer.wrap(data);
        RSBuffer nc4 = RSBuffer.wrap(data);
        RSBuffer nc5 = RSBuffer.wrap(data);
        RSBuffer nc6 = RSBuffer.wrap(data);
        RSBuffer nc7 = RSBuffer.wrap(data);
        nc1.setPosition(data.length - 23);
        vertices = nc1.readUShort();
        faces = nc1.readUShort();
        anInt1642 = nc1.readUByte();
        int flags = nc1.readUByte();
        int priority_opcode = nc1.readUByte();
        int alpha_opcode = nc1.readUByte();
        int tSkin_opcode = nc1.readUByte();
        int texture_opcode = nc1.readUByte();
        int vSkin_opcode = nc1.readUByte();
        int j3 = nc1.readUShort();
        int k3 = nc1.readUShort();
        int l3 = nc1.readUShort();
        int i4 = nc1.readUShort();
        int j4 = nc1.readUShort();
        int texture_id = 0;
        int texture_ = 0;
        int texture__ = 0;
        int face;
        colors = new short[faces];
        if (anInt1642 > 0) {
            texture_type = new byte[anInt1642];
            nc1.setPosition(0);
            for (face = 0; face < anInt1642; face++) {
                byte opcode = texture_type[face] = nc1.readByte();
                if (opcode == 0) {
                    texture_id++;
                }

                if (opcode >= 1 && opcode <= 3) {
                    texture_++;
                }
                if (opcode == 2) {
                    texture__++;
                }
            }
        }
        int pos;
        pos = anInt1642;
        int vertexMod_offset = pos;
        pos += vertices;

        int drawTypeBasePos = pos;
        if (flags == 1)
            pos += faces;

        int faceMeshLink_offset = pos;
        pos += faces;

        int facePriorityBasePos = pos;
        if (priority_opcode == 255)
            pos += faces;

        int tSkinBasePos = pos;
        if (tSkin_opcode == 1)
            pos += faces;

        int vSkinBasePos = pos;
        if (vSkin_opcode == 1)
            pos += vertices;

        int alphaBasePos = pos;
        if (alpha_opcode == 1)
            pos += faces;

        int faceVPoint_offset = pos;
        pos += i4;

        int textureIdBasePos = pos;
        if (texture_opcode == 1)
            pos += faces * 2;

        int textureBasePos = pos;
        pos += j4;

        int color_offset = pos;
        pos += faces * 2;

        int vertexX_offset = pos;
        pos += j3;

        int vertexY_offset = pos;
        pos += k3;

        int vertexZ_offset = pos;
        pos += l3;

        int mainBuffer_offset = pos;
        pos += texture_id * 6;

        int firstBuffer_offset = pos;
        pos += texture_ * 6;

        int secondBuffer_offset = pos;
        pos += texture_ * 6;

        int thirdBuffer_offset = pos;
        pos += texture_ * 2;

        int fourthBuffer_offset = pos;
        pos += texture_;

        int fifthBuffer_offset = pos;
        pos += texture_ * 2 + texture__ * 2;
        verticesParticle = new int[vertices];
        verticesX = new int[vertices];
        verticesY = new int[vertices];
        verticesZ = new int[vertices];
        facesA = new int[faces];
        facesB = new int[faces];
        facesC = new int[faces];
        if (vSkin_opcode == 1)
            anIntArray1655 = new int[vertices];

        if (flags == 1)
            anIntArray1637 = new int[faces];

        if (priority_opcode == 255)
            anIntArray1638 = new byte[faces];
        else
            anInt1641 = (byte) priority_opcode;

        if (alpha_opcode == 1)
            anIntArray1639 = new int[faces];

        if (tSkin_opcode == 1)
            anIntArray1656 = new int[faces];

        if (texture_opcode == 1)
            texture = new short[faces];

        if (texture_opcode == 1 && anInt1642 > 0)
            texture_coordinates = new byte[faces];

        if (anInt1642 > 0) {
            anIntArray1643 = new short[anInt1642];
            anIntArray1644 = new short[anInt1642];
            anIntArray1645 = new short[anInt1642];
        }
        nc1.setPosition(vertexMod_offset);
        nc2.setPosition(vertexX_offset);
        nc3.setPosition(vertexY_offset);
        nc4.setPosition(vertexZ_offset);
        nc5.setPosition(vSkinBasePos);
        int start_x = 0;
        int start_y = 0;
        int start_z = 0;
        for (int point = 0; point < vertices; point++) {
            int flag = nc1.readUByte();
            int x = 0;
            if ((flag & 1) != 0) {
                x = nc2.readUSmart();
            }
            int y = 0;
            if ((flag & 2) != 0) {
                y = nc3.readUSmart();

            }
            int z = 0;
            if ((flag & 4) != 0) {
                z = nc4.readUSmart();
            }
            verticesX[point] = start_x + x;
            verticesY[point] = start_y + y;
            verticesZ[point] = start_z + z;
            start_x = verticesX[point];
            start_y = verticesY[point];
            start_z = verticesZ[point];
            if (anIntArray1655 != null)
                anIntArray1655[point] = nc5.readUByte();

        }
        nc1.setPosition(color_offset);
        nc2.setPosition(drawTypeBasePos);
        nc3.setPosition(facePriorityBasePos);
        nc4.setPosition(alphaBasePos);
        nc5.setPosition(tSkinBasePos);
        nc6.setPosition(textureIdBasePos);
        nc7.setPosition(textureBasePos);
        for (face = 0; face < faces; face++) {
            colors[face] = (short) nc1.readUShort();
            if (flags == 1) {
                anIntArray1637[face] = nc2.readByte();
            }
            if (priority_opcode == 255) {
                anIntArray1638[face] = nc3.readByte();
            }
            if (alpha_opcode == 1) {
                anIntArray1639[face] = nc4.readByte();
                if (anIntArray1639[face] < 0)
                    anIntArray1639[face] = (256 + anIntArray1639[face]);

            }
            if (tSkin_opcode == 1)
                anIntArray1656[face] = nc5.readUByte();

            if (texture_opcode == 1) {
                texture[face] = (short) (nc6.readUShort() - 1);
                if (texture[face] >= 0) {
                    if (anIntArray1637 != null) {
                        if (anIntArray1637[face] < 2 && colors[face] != 127 && colors[face] != -27075) {
                            texture[face] = -1;
                        }
                    }
                }
                if (texture[face] != -1)
                    colors[face] = 127;
            }
            if (texture_coordinates != null && texture[face] != -1) {
                texture_coordinates[face] = (byte) (nc7.readUByte() - 1);
            }
        }
        nc1.setPosition(faceVPoint_offset);
        nc2.setPosition(faceMeshLink_offset);
        int coordinate_a = 0;
        int coordinate_b = 0;
        int coordinate_c = 0;
        int last_coordinate = 0;
        for (face = 0; face < faces; face++) {
            int opcode = nc2.readUByte();
            if (opcode == 1) {
                coordinate_a = nc1.readUSmart() + last_coordinate;
                last_coordinate = coordinate_a;
                coordinate_b = nc1.readUSmart() + last_coordinate;
                last_coordinate = coordinate_b;
                coordinate_c = nc1.readUSmart() + last_coordinate;
                last_coordinate = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
            if (opcode == 2) {
                coordinate_b = coordinate_c;
                coordinate_c = nc1.readUSmart() + last_coordinate;
                last_coordinate = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
            if (opcode == 3) {
                coordinate_a = coordinate_c;
                coordinate_c = nc1.readUSmart() + last_coordinate;
                last_coordinate = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
            if (opcode == 4) {
                int l14 = coordinate_a;
                coordinate_a = coordinate_b;
                coordinate_b = l14;
                coordinate_c = nc1.readUSmart() + last_coordinate;
                last_coordinate = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
        }
        nc1.setPosition(mainBuffer_offset);
        nc2.setPosition(firstBuffer_offset);
        nc3.setPosition(secondBuffer_offset);
        nc4.setPosition(thirdBuffer_offset);
        nc5.setPosition(fourthBuffer_offset);
        nc6.setPosition(fifthBuffer_offset);

        for (face = 0; face < anInt1642; face++) {
            int opcode = texture_type[face] & 0xff;
            if (opcode == 0) {
                anIntArray1643[face] = (short) nc1.readUShort();
                anIntArray1644[face] = (short) nc1.readUShort();
                anIntArray1645[face] = (short) nc1.readUShort();
            }
            if (opcode == 1) {
                anIntArray1643[face] = (short) nc2.readUShort();
                anIntArray1644[face] = (short) nc2.readUShort();
                anIntArray1645[face] = (short) nc2.readUShort();
            }
            if (opcode == 2) {
                anIntArray1643[face] = (short) nc2.readUShort();
                anIntArray1644[face] = (short) nc2.readUShort();
                anIntArray1645[face] = (short) nc2.readUShort();
            }
            if (opcode == 3) {
                anIntArray1643[face] = (short) nc2.readUShort();
                anIntArray1644[face] = (short) nc2.readUShort();
                anIntArray1645[face] = (short) nc2.readUShort();
            }
        }
        nc1.setPosition(pos);
        face = nc1.readUByte();
    }

    private void decodeOldModel(byte[] data) {
        boolean has_face_type = false;
        boolean has_texture_type = false;
        RSBuffer stream = RSBuffer.wrap(data);
        RSBuffer stream1 = RSBuffer.wrap(data);
        RSBuffer stream2 = RSBuffer.wrap(data);
        RSBuffer stream3 = RSBuffer.wrap(data);
        RSBuffer stream4 = RSBuffer.wrap(data);
        stream.setPosition(data.length - 18);
        vertices = stream.readUShort();
        faces = stream.readUShort();
        anInt1642 = stream.readUByte();
        int type_opcode = stream.readUByte();
        int priority_opcode = stream.readUByte();
        int alpha_opcode = stream.readUByte();
        int tSkin_opcode = stream.readUByte();
        int vSkin_opcode = stream.readUByte();
        int i_254_ = stream.readUShort();
        int i_255_ = stream.readUShort();
        int i_256_ = stream.readUShort();
        int i_257_ = stream.readUShort();
        int i_258_ = 0;

        int i_259_ = i_258_;
        i_258_ += vertices;

        int i_260_ = i_258_;
        i_258_ += faces;

        int i_261_ = i_258_;
        if (priority_opcode == 255)
            i_258_ += faces;

        int i_262_ = i_258_;
        if (tSkin_opcode == 1)
            i_258_ += faces;

        int i_263_ = i_258_;
        if (type_opcode == 1)
            i_258_ += faces;

        int i_264_ = i_258_;
        if (vSkin_opcode == 1)
            i_258_ += vertices;

        int i_265_ = i_258_;
        if (alpha_opcode == 1)
            i_258_ += faces;

        int i_266_ = i_258_;
        i_258_ += i_257_;

        int i_267_ = i_258_;
        i_258_ += faces * 2;

        int i_268_ = i_258_;
        i_258_ += anInt1642 * 6;

        int i_269_ = i_258_;
        i_258_ += i_254_;

        int i_270_ = i_258_;
        i_258_ += i_255_;

        int i_271_ = i_258_;
        i_258_ += i_256_;

        verticesParticle = new int[vertices];
        verticesX = new int[vertices];
        verticesY = new int[vertices];
        verticesZ = new int[vertices];
        facesA = new int[faces];
        facesB = new int[faces];
        facesC = new int[faces];
        if (anInt1642 > 0) {
            texture_type = new byte[anInt1642];
            anIntArray1643 = new short[anInt1642];
            anIntArray1644 = new short[anInt1642];
            anIntArray1645 = new short[anInt1642];
        }

        if (vSkin_opcode == 1)
            anIntArray1655 = new int[vertices];

        if (type_opcode == 1) {
            anIntArray1637 = new int[faces];
            texture_coordinates = new byte[faces];
            texture = new short[faces];
        }

        if (priority_opcode == 255)
            anIntArray1638 = new byte[faces];
        else
            anInt1641 = (byte) priority_opcode;

        if (alpha_opcode == 1)
            anIntArray1639 = new int[faces];

        if (tSkin_opcode == 1)
            anIntArray1656 = new int[faces];

        colors = new short[faces];
        stream.setPosition(i_259_);
        stream1.setPosition(i_269_);
        stream2.setPosition(i_270_);
        stream3.setPosition(i_271_);
        stream4.setPosition(i_264_);
        int start_x = 0;
        int start_y = 0;
        int start_z = 0;
        for (int point = 0; point < vertices; point++) {
            int flag = stream.readUByte();
            int x = 0;
            if ((flag & 0x1) != 0)
                x = stream1.readUSmart();
            int y = 0;
            if ((flag & 0x2) != 0)
                y = stream2.readUSmart();
            int z = 0;
            if ((flag & 0x4) != 0)
                z = stream3.readUSmart();

            verticesX[point] = start_x + x;
            verticesY[point] = start_y + y;
            verticesZ[point] = start_z + z;
            start_x = verticesX[point];
            start_y = verticesY[point];
            start_z = verticesZ[point];
            if (vSkin_opcode == 1)
                anIntArray1655[point] = stream4.readUByte();

        }
        stream.setPosition(i_267_);
        stream1.setPosition(i_263_);
        stream2.setPosition(i_261_);
        stream3.setPosition(i_265_);
        stream4.setPosition(i_262_);

        for (int face = 0; face < faces; face++) {
            colors[face] = (short) stream.readUShort();
            if (type_opcode == 1) {
                int flag = stream1.readUByte();
                if ((flag & 0x1) == 1) {
                    anIntArray1637[face] = 1;
                    has_face_type = true;
                } else {
                    anIntArray1637[face] = 0;
                }

                if ((flag & 0x2) != 0) {
                    texture_coordinates[face] = (byte) (flag >> 2);
                    texture[face] = colors[face];
                    colors[face] = 127;
                    if (texture[face] != -1)
                        has_texture_type = true;
                } else {
                    texture_coordinates[face] = -1;
                    texture[face] = -1;
                }
            }
            if (priority_opcode == 255)
                anIntArray1638[face] = stream2.readByte();

            if (alpha_opcode == 1) {
                anIntArray1639[face] = stream3.readByte();
                if (anIntArray1639[face] < 0)
                    anIntArray1639[face] = (256 + anIntArray1639[face]);

            }
            if (tSkin_opcode == 1)
                anIntArray1656[face] = stream4.readUByte();

        }

        stream.setPosition(i_266_);
        stream1.setPosition(i_260_);

        int coordinate_a = 0;
        int coordinate_b = 0;
        int coordinate_c = 0;
        int offset = 0;
        int coordinate;
        for (int face = 0; face < faces; face++) {
            int opcode = stream1.readUByte();
            if (opcode == 1) {
                coordinate_a = (stream.readUSmart() + offset);
                offset = coordinate_a;
                coordinate_b = (stream.readUSmart() + offset);
                offset = coordinate_b;
                coordinate_c = (stream.readUSmart() + offset);
                offset = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
            if (opcode == 2) {
                coordinate_b = coordinate_c;
                coordinate_c = (stream.readUSmart() + offset);
                offset = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
            if (opcode == 3) {
                coordinate_a = coordinate_c;
                coordinate_c = (stream.readUSmart() + offset);
                offset = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
            if (opcode == 4) {
                coordinate = coordinate_a;
                coordinate_a = coordinate_b;
                coordinate_b = coordinate;
                coordinate_c = (stream.readUSmart() + offset);
                offset = coordinate_c;
                facesA[face] = coordinate_a;
                facesB[face] = coordinate_b;
                facesC[face] = coordinate_c;
            }
        }

        stream.setPosition(i_268_);

        for (int face = 0; face < anInt1642; face++) {
            texture_type[face] = 0;
            anIntArray1643[face] = (short) stream.readUShort();
            anIntArray1644[face] = (short) stream.readUShort();
            anIntArray1645[face] = (short) stream.readUShort();
        }
        if (texture_coordinates != null) {
            boolean textured = false;
            for (int face = 0; face < faces; face++) {
                coordinate = texture_coordinates[face] & 0xff;
                if (coordinate != 255) {
                    if (((anIntArray1643[coordinate] & 0xffff) == facesA[face]) && ((anIntArray1644[coordinate] & 0xffff) == facesB[face]) && ((anIntArray1645[coordinate] & 0xffff) == facesC[face])) {
                        texture_coordinates[face] = -1;
                    } else {
                        textured = true;
                    }
                }
            }
            if (!textured)
                texture_coordinates = null;
        }
        if (!has_texture_type)
            texture = null;

        if (!has_face_type)
            anIntArray1637 = null;

    }

    public void translate(int i, int j, int l) {
        for (int i1 = 0; i1 < vertices; i1++) {
            verticesX[i1] += i;
            verticesY[i1] += j;
            verticesZ[i1] += l;
        }
    }

    public void recolor(int found, int replace) {
        if (colors != null) {
            for (int face = 0; face < faces; face++) {
                if (colors[face] == (short) found) {
                    colors[face] = (short) replace;
                }
            }
        }
    }

    public void retexture(short found, short replace) {
        if (texture != null) {
            for (int face = 0; face < faces; face++)
                if (texture[face] == found) {
                    texture[face] = replace;
                }
        }
    }

    public void scale(int x, int y, int z) {
        for (int i = 0; i < vertices; i++) {
            verticesX[i] = (verticesX[i] * x) / 128;
            verticesY[i] = (verticesY[i] * z) / 128;
            verticesZ[i] = (verticesZ[i] * y) / 128;
        }
    }

    public void rotateClockwise() {
        for (int index = 0; index < vertices; index++) {
            int x = verticesX[index];
            verticesX[index] = verticesZ[index];
            verticesZ[index] = -x;
        }
    }

    public void rotateY90Clockwise() {
        for (int index = 0; index < vertices; index++) {
            int y = verticesY[index];
            verticesY[index] = verticesZ[index];
            verticesZ[index] = -y;
        }
    }

    public int[] verticesParticle;
    public short[] texture;
    public byte[] texture_coordinates;
    public byte[] texture_type;
    public int vertices;
    public int verticesX[];
    public int verticesY[];
    public int verticesZ[];
    public int faces;
    public int facesA[];
    public int facesB[];
    public int facesC[];
    public int anIntArray1637[];
    public byte anIntArray1638[];
    public int anIntArray1639[];
    public short colors[];
    public byte anInt1641;
    public int anInt1642;
    public short anIntArray1643[];
    public short anIntArray1644[];
    public short anIntArray1645[];
    public int anIntArray1655[];
    public int anIntArray1656[];

}