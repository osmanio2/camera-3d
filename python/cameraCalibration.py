#!/usr/bin/env python

import numpy as np
import cv2
import time

square_size = 1.0
numOfimages = 20
pattern_size = (9, 7)
pattern_points = np.zeros( (np.prod(pattern_size), 3), np.float32 )
pattern_points[:,:2] = np.indices(pattern_size).T.reshape(-1, 2)
pattern_points *= square_size

obj_points = []
img_points = []
h, w = 0, 0
cap = cv2.VideoCapture(0)
countShots = 0
while(True):
        # Capture frame-by-frame
    ret, frame = cap.read()
        # Our operations on the frame come here
    img = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        #print 'processing %s...' % fn,
        #img = cv2.imread(fn, 0)
    h, w = img.shape[:2]

    key = cv2.waitKey(1)
    found, corners = cv2.findChessboardCorners(img, pattern_size)
    if found:
        #print 'chessboard found'
        term = ( cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_COUNT, 30, 0.1 )
        cv2.cornerSubPix(img, corners, (5, 5), (-1, -1), term)
        cv2.drawChessboardCorners(img, pattern_size, corners, found)
    cv2.imshow('original', img)
    if not found:
        #print 'chessboard not found'
        continue
    if key & 0xFF == ord('q'):
        img_points.append(corners.reshape(-1, 2))
        obj_points.append(pattern_points)
        countShots += 1
        print countShots
        if countShots == numOfimages: break

print 'ok'
    # When everything done, release the capture
cap.release()
rms, camera_matrix, dist_coefs, rvecs, tvecs = cv2.calibrateCamera(obj_points, img_points, (w, h))
print "RMS:", rms
print "camera matrix:\n", camera_matrix
print "distortion coefficients: ", dist_coefs.ravel()
cv2.destroyAllWindows()
