/*
 *     Copyright (C) 2019  Filippo Scognamiglio
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#ifndef LIBRETRODROID_FPSSYNC_H
#define LIBRETRODROID_FPSSYNC_H

#include <chrono>
#include <thread>

namespace libretrodroid {

typedef std::chrono::steady_clock::time_point TimePoint;
typedef std::chrono::duration<long, std::micro> Duration;

class FPSSync {
public:
    FPSSync(double contentRefreshRate, double screenRefreshRate);
    ~FPSSync() { }

    void reset();
    unsigned advanceFrames();
    void wait();
    double getTimeStretchFactor();
private:

    double screenRefreshRate;
    double contentRefreshRate;
    bool useVSync;
    const double FPS_TOLERANCE = 5;

    const TimePoint MIN_TIME = TimePoint::min();
    void start();

    TimePoint lastFrame = MIN_TIME;
    Duration sampleInterval;
};

}


#endif //LIBRETRODROID_FPSSYNC_H
