#!/usr/bin/env python

'''
Simple example of stereo image matching and point cloud generation.

Resulting .ply file cam be easily viewed using MeshLab ( http://meshlab.sourceforge.net/ )
'''

import numpy as np
import cv2

K = np.float32([[ 597.31632592, 0., 334.37227826], [0., 566.12785006, 311.72572164], [0., 0., 1.]])
d = np.array([-9.05381703e-02, 1.25206118e+00, 5.68513953e-02, 1.64754677e-03, -2.75910906e+00, 0.0, 0.0, 0.0])\
    .reshape(1, 8)
K_inv = np.linalg.inv(K)
ply_header = '''ply
format ascii 1.0
element vertex %(vert_num)d
property float x
property float y
property float z
property uchar red
property uchar green
property uchar blue
end_header
'''
def in_front_of_both_cameras(first_points, second_points, rot, trans):
    # check if the point correspondences are in front of both images
    rot_inv = rot
    for first, second in zip(first_points, second_points):
        first_z = np.dot(rot[0, :] - second[0]*rot[2, :], trans) / np.dot(rot[0, :] - second[0]*rot[2, :], second)
        first_3d_point = np.array([first[0] * first_z, second[0] * first_z, first_z])
        second_3d_point = np.dot(rot.T, first_3d_point) - np.dot(rot.T, trans)

        if first_3d_point[2] < 0 or second_3d_point[2] < 0:
            return False

    return True

def write_ply(fn, verts, colors):
    verts = verts.reshape(-1, 3)
    colors = colors.reshape(-1, 3)
    verts = np.hstack([verts, colors])
    with open(fn, 'w') as f:
        f.write(ply_header % dict(vert_num=len(verts)))
        np.savetxt(f, verts, '%f %f %f %d %d %d')


if __name__ == '__main__':
    print 'loading images...'
#    imgL = cv2.pyrDown( cv2.imread('../gpu/aloeL.jpg') )  # downscale images for faster processing
#    imgR = cv2.pyrDown( cv2.imread('../gpu/aloeR.jpg') )

    cap = cv2.VideoCapture(0)
    imgCount = 0
    while (True):
        # Capture frame-by-frame
        ret, frame = cap.read()
        # Display the resulting frame
        cv2.imshow('frame', frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            if imgCount == 0:
                imgl = frame
                print "Image one captured"
            elif imgCount == 1:
                imgr = frame
                print "Image two captured"
            imgCount += 1

            if imgCount == 2: break
    imgL = cv2.undistort(imgl, K, d)
    imgR = cv2.undistort(imgr, K, d)
    # extract key points and descriptors from both images
    detector = cv2.SURF(250)
    first_key_points, first_descriptors = detector.detectAndCompute(imgL, None)
    second_key_points, second_descriptos = detector.detectAndCompute(imgR, None)

    # match descriptors
    matcher = cv2.BFMatcher(cv2.NORM_L1, True)
    matches = matcher.match(first_descriptors, second_descriptos)

    # generate lists of point correspondences
    first_match_points = np.zeros((len(matches), 2), dtype=np.float32)
    second_match_points = np.zeros_like(first_match_points)
    for i in range(len(matches)):
        first_match_points[i] = first_key_points[matches[i].queryIdx].pt
        second_match_points[i] = second_key_points[matches[i].trainIdx].pt

    # estimate fundamental matrix
    F, mask = cv2.findFundamentalMat(first_match_points, second_match_points, cv2.FM_RANSAC, 0.1, 0.99)

    # decompose into the essential matrix
    E = K.T.dot(F).dot(K)

    # decompose essential matrix into R, t (See Hartley and Zisserman 9.13)
    U, S, Vt = np.linalg.svd(E)
    W = np.array([0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0]).reshape(3, 3)
    # iterate over all point correspondences used in the estimation of the fundamental matrix
    first_inliers = []
    second_inliers = []
    for i in range(len(mask)):
        if mask[i]:
            # normalize and homogenize the image coordinates
            first_inliers.append(K_inv.dot([first_match_points[i][0], first_match_points[i][1], 1.0]))
            second_inliers.append(K_inv.dot([second_match_points[i][0], second_match_points[i][1], 1.0]))

    # Determine the correct choice of second camera matrix
    # only in one of the four configurations will all the points be in front of both cameras
    # First choice: R = U * Wt * Vt, T = +u_3 (See Hartley Zisserman 9.19)
    R = U.dot(W).dot(Vt)
    T = U[:, 2]
    if not in_front_of_both_cameras(first_inliers, second_inliers, R, T):

        # Second choice: R = U * W * Vt, T = -u_3
        T = - U[:, 2]
        if not in_front_of_both_cameras(first_inliers, second_inliers, R, T):

            # Third choice: R = U * Wt * Vt, T = u_3
            R = U.dot(W.T).dot(Vt)
            T = U[:, 2]

            if not in_front_of_both_cameras(first_inliers, second_inliers, R, T):
                # Fourth choice: R = U * Wt * Vt, T = -u_3
                T = - U[:, 2]

            # perform the rectification
            R1, R2, P1, P2, Q, roi1, roi2 = cv2.stereoRectify(K, d, K, d, imgl.shape[:2], R, T, alpha=1.0)
            mapx1, mapy1 = cv2.initUndistortRectifyMap(K, d, R1, K, imgl.shape[:2], cv2.CV_32F)
            mapx2, mapy2 = cv2.initUndistortRectifyMap(K, d, R2, K, imgr.shape[:2], cv2.CV_32F)
            img_rect1 = cv2.remap(imgl, mapx1, mapy1, cv2.INTER_LINEAR)
            img_rect2 = cv2.remap(imgr, mapx2, mapy2, cv2.INTER_LINEAR)

            # draw the images side by side
            total_size = (max(img_rect1.shape[0], img_rect2.shape[0]), img_rect1.shape[1] + img_rect2.shape[1], 3)
            img = np.zeros(total_size, dtype=np.uint8)
            img[:img_rect1.shape[0], :img_rect1.shape[1]] = img_rect1
            img[:img_rect2.shape[0], img_rect1.shape[1]:] = img_rect2

            # draw horizontal lines every 25 px accross the side by side image
            for i in range(20, img.shape[0], 25):
                cv2.line(img, (0, i), (img.shape[1], i), (255, 0, 0))

            cv2.imshow('rectified', img)
            cv2.waitKey(0)
    # disparity range is tuned for 'aloe' image pair
    window_size = 5
    min_disp = 20
    num_disp = 180-min_disp
    stereo = cv2.StereoSGBM(minDisparity = min_disp,
        numDisparities = num_disp,
        SADWindowSize = window_size,
        uniquenessRatio = 10,
        speckleWindowSize = 100,
        speckleRange = 2,
        disp12MaxDiff = -1,
        P1 = 8*3*window_size**2,
        P2 = 32*3*window_size**2,
        fullDP = False
    )
    #print " disparity-to-depth mapping matrix :\n", Q
    print 'computing disparity...'
    disp = stereo.compute(imgL, imgR).astype(np.float32) / 16.0

    print 'generating 3d point cloud...',

    h, w = imgL.shape[:2]
    f = 0.8 * w  # guess for focal length
    Q = np.float32([[1, 0, 0, -0.5 * w],
                    [0, -1, 0, 0.5 * h],  # turn points 180 deg around x-axis,
                    [0, 0, 0, -f],  # so that y-axis looks up
                    [0, 0, 1, 0]])

    points = cv2.reprojectImageTo3D(disp, Q)
    colors = cv2.cvtColor(imgL, cv2.COLOR_BGR2RGB)
    mask = disp > disp.min()
    out_points = points[mask]
    print out_points
    out_colors = colors[mask]
    out_fn = 'out.ply'
    write_ply('out.ply', out_points, out_colors)
    print '%s saved' % 'out.ply'
    cap.release()
    cv2.imshow('left', imgL)
    cv2.imshow('disparity', (disp-min_disp)/num_disp)
    cv2.waitKey()
    cv2.destroyAllWindows()
