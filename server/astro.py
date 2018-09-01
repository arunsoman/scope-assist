from datetime import datetime

from astropy.coordinates import Angle
from astropy import units as u
from astropy.coordinates import SkyCoord
from astropy.coordinates import EarthLocation
from astropy.time import Time
from astropy.coordinates import AltAz

class AstroProxy:
    def __init__(self):
        self.object = None
        self.observer = None

    def setObject(self, name):
        self.object = SkyCoord.from_name(name)

    def setObserver(self, lat, lon, height):
        self.observer = EarthLocation(lat,lon,height )

    def getAltAz(self):
        observing_time = Time(datetime.utcnow(), scale='utc')
        print observing_time
        aa = AltAz(location=self.observer, obstime=observing_time)
        to = self.object.transform_to(aa)
        return Angle(to.alt), Angle(to.az)

    def getAltAzPolaris(self):
        self.setObject('Polaris')
        return self.getAltAz()

def f():
    o = AstroProxy()
    o.setObserver('8d31m26.9004s','76d56m11.8968s', 22)
    print(o.getAltAzPolaris())

